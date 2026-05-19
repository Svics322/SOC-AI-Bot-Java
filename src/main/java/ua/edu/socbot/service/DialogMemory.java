package ua.edu.socbot.service;

import org.springframework.stereotype.Component;
import ua.edu.socbot.dto.IncidentCard;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DialogMemory {
    private String lastIntent, lastSeverity, lastMessage;
    private Map<String, List<String>> lastEntities = Map.of();
    public void update(String intent, String severity, String message, Map<String, List<String>> entities) {
        lastIntent = intent; lastSeverity = severity; lastMessage = message; lastEntities = entities;
    }
    public IncidentCard createIncidentCard() {
        return new IncidentCard(lastIntent == null ? "unknown" : lastIntent, lastSeverity == null ? "Невідомий" : lastSeverity,
                lastMessage == null ? "Опис відсутній" : lastMessage, lastEntities, LocalDateTime.now());
    }
}
