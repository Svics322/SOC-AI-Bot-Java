package ua.edu.socbot.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TextPreprocessor {
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();
        String normalized = text.toLowerCase(Locale.ROOT)
                .replace('’', '\'')
                .replaceAll("[^a-zа-яіїєґ0-9@._:/-]+", " ");
        String[] parts = normalized.trim().split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) if (!part.isBlank()) tokens.add(part);
        return tokens;
    }
}
