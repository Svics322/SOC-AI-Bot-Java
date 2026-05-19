package ua.edu.socbot.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record IncidentCard(
        String incidentType,
        String severity,
        String description,
        Map<String, List<String>> entities,
        LocalDateTime createdAt
) { }
