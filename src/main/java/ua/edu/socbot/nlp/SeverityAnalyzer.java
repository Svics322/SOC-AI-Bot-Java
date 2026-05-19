package ua.edu.socbot.nlp;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SeverityAnalyzer {
    private static final Map<String, String> BASE = Map.ofEntries(
            Map.entry("ransomware_alert", "Критичний"), Map.entry("data_leak_suspicion", "Високий"),
            Map.entry("account_compromise", "Високий"), Map.entry("bruteforce_detected", "Середній"),
            Map.entry("malware_suspicion", "Середній"), Map.entry("phishing_report", "Середній"),
            Map.entry("suspicious_network_activity", "Середній"), Map.entry("create_incident_ticket", "Середній"),
            Map.entry("greeting", "Інформаційний"), Map.entry("unknown", "Невідомий"));
    public String analyze(String intent, String text, Map<String, List<String>> entities) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("критично") || lower.contains("негайно") || lower.contains("викуп") || lower.contains("зашифрован") || lower.contains("персональні дані")) return "Критичний";
        if ("bruteforce_detected".equals(intent) && (lower.contains("admin") || lower.contains("administrator") || lower.contains("3389"))) return "Високий";
        if (entities.containsKey("severity_marker")) return "Високий";
        return BASE.getOrDefault(intent, "Невідомий");
    }
}
