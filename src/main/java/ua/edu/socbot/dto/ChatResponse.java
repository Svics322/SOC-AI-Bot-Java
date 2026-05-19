package ua.edu.socbot.dto;

import java.util.List;
import java.util.Map;

public record ChatResponse(
        String answer,
        String intent,
        double confidence,
        String severity,
        Map<String, List<String>> entities,
        List<String> recommendations,
        List<IntentScore> topIntents,
        IncidentCard incidentCard
) { }
