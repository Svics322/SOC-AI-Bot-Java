package ua.edu.socbot.service;

import org.springframework.stereotype.Service;
import ua.edu.socbot.dto.*;
import ua.edu.socbot.model.KnowledgeItem;
import ua.edu.socbot.nlp.*;
import java.util.*;

@Service
public class BotService {
    private final IntentClassifier classifier; private final EntityExtractor extractor; private final SeverityAnalyzer severityAnalyzer;
    private final KnowledgeBaseService kb; private final DialogMemory memory;
    public BotService(IntentClassifier classifier, EntityExtractor extractor, SeverityAnalyzer severityAnalyzer, KnowledgeBaseService kb, DialogMemory memory) {
        this.classifier = classifier; this.extractor = extractor; this.severityAnalyzer = severityAnalyzer; this.kb = kb; this.memory = memory;
    }
    public ChatResponse process(String message) {
        if (message == null || message.isBlank()) return new ChatResponse("Опишіть, будь ласка, подію або підозрілу активність.", "empty", 0, "Невідомий", Map.of(), List.of(), List.of(), null);
        IntentPrediction pred = classifier.predict(message);
        Map<String, List<String>> entities = extractor.extract(message);
        String severity = severityAnalyzer.analyze(pred.intent(), message, entities);
        KnowledgeItem item = kb.findByIntent(pred.intent());
        StringBuilder answer = new StringBuilder();
        answer.append(item.getResponse()).append("\n\nПопередня класифікація: ").append(item.getTitle()).append(".\nРівень критичності: ").append(severity).append(".");
        if (requiresDetails(pred.intent()) && lacksEntities(entities)) answer.append("\n\nУточніть IP, домен, email, URL, порт або назву хоста, якщо вони відомі.");
        memory.update(pred.intent(), severity, message, entities);
        String lower = message.toLowerCase(Locale.ROOT);
        IncidentCard card = ("create_incident_ticket".equals(pred.intent()) || lower.contains("заяв") || lower.contains("тік") || lower.contains("картк") || lower.contains("ескал")) ? memory.createIncidentCard() : null;
        return new ChatResponse(answer.toString(), pred.intent(), pred.confidence(), severity, entities, item.getRecommendations(), pred.topIntents(), card);
    }
    private boolean requiresDetails(String intent) { return List.of("phishing_report", "malware_suspicion", "bruteforce_detected", "suspicious_network_activity").contains(intent); }
    private boolean lacksEntities(Map<String, List<String>> e) { return !(e.containsKey("ip_address") || e.containsKey("email") || e.containsKey("domain") || e.containsKey("url") || e.containsKey("hostname") || e.containsKey("port")); }
}
