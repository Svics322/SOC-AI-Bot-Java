package ua.edu.socbot.nlp;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ua.edu.socbot.dto.IntentScore;
import ua.edu.socbot.model.TrainingExample;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class IntentClassifier {
    private final TextPreprocessor preprocessor = new TextPreprocessor();
    private final List<TrainingExample> examples = new ArrayList<>();
    private final Map<String, Map<String, Double>> intentCentroids = new HashMap<>();
    private final Map<String, Double> idf = new HashMap<>();
    private final Set<String> vocabulary = new HashSet<>();

    @PostConstruct
    public void init() { loadDataset(); train(); }

    public IntentPrediction predict(String text) {
        Map<String, Double> vector = toTfidfVector(text);
        List<IntentScore> scores = intentCentroids.entrySet().stream()
                .map(e -> new IntentScore(e.getKey(), cosine(vector, e.getValue())))
                .sorted(Comparator.comparingDouble(IntentScore::score).reversed())
                .toList();
        if (scores.isEmpty()) return new IntentPrediction("unknown", 0.0, List.of());
        List<IntentScore> probabilities = softmax(scores);
        IntentScore best = probabilities.get(0);
        String intent = best.intent();
        double confidence = best.score();
        if (confidence < 0.24) intent = "unknown";
        return new IntentPrediction(intent, confidence, probabilities.stream().limit(3).toList());
    }

    private void loadDataset() {
        try {
            ClassPathResource resource = new ClassPathResource("data/intents_dataset.csv");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                while ((line = reader.readLine()) != null) {
                    List<String> parts = parseCsvLine(line);
                    if (parts.size() >= 2) examples.add(new TrainingExample(parts.get(0).trim(), parts.get(1).trim()));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Не вдалося завантажити data/intents_dataset.csv", e);
        }
    }

    private void train() {
        Map<String, Integer> df = new HashMap<>();
        for (TrainingExample ex : examples) {
            Set<String> unique = new HashSet<>(preprocessor.tokenize(ex.text()));
            unique.forEach(t -> df.merge(t, 1, Integer::sum));
            vocabulary.addAll(unique);
        }
        int n = examples.size();
        for (String term : vocabulary) idf.put(term, Math.log((n + 1.0) / (df.getOrDefault(term, 0) + 1.0)) + 1.0);
        Map<String, List<Map<String, Double>>> byIntent = new HashMap<>();
        for (TrainingExample ex : examples) byIntent.computeIfAbsent(ex.intent(), k -> new ArrayList<>()).add(toTfidfVector(ex.text()));
        for (var e : byIntent.entrySet()) intentCentroids.put(e.getKey(), average(e.getValue()));
    }

    private Map<String, Double> toTfidfVector(String text) {
        List<String> tokens = preprocessor.tokenize(text);
        Map<String, Long> counts = tokens.stream().collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        double total = Math.max(tokens.size(), 1);
        Map<String, Double> v = new HashMap<>();
        for (var e : counts.entrySet()) v.put(e.getKey(), (e.getValue() / total) * idf.getOrDefault(e.getKey(), 1.0));
        return v;
    }

    private Map<String, Double> average(List<Map<String, Double>> vectors) {
        Map<String, Double> out = new HashMap<>();
        for (Map<String, Double> v : vectors) for (var e : v.entrySet()) out.merge(e.getKey(), e.getValue(), Double::sum);
        out.replaceAll((k, val) -> val / Math.max(vectors.size(), 1));
        return out;
    }

    private double cosine(Map<String, Double> a, Map<String, Double> b) {
        double dot = 0, an = 0, bn = 0;
        for (double v : a.values()) an += v * v;
        for (double v : b.values()) bn += v * v;
        for (var e : a.entrySet()) dot += e.getValue() * b.getOrDefault(e.getKey(), 0.0);
        return an == 0 || bn == 0 ? 0 : dot / (Math.sqrt(an) * Math.sqrt(bn));
    }

    private List<IntentScore> softmax(List<IntentScore> scores) {
        double max = scores.stream().mapToDouble(IntentScore::score).max().orElse(0);
        double sum = scores.stream().mapToDouble(s -> Math.exp((s.score() - max) * 8)).sum();
        return scores.stream()
                .map(s -> new IntentScore(s.intent(), Math.exp((s.score() - max) * 8) / sum))
                .sorted(Comparator.comparingDouble(IntentScore::score).reversed())
                .toList();
    }

    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') quote = !quote;
            else if (c == ',' && !quote) { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        out.add(cur.toString());
        return out;
    }
}
