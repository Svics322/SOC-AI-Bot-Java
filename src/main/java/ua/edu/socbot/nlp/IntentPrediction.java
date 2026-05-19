package ua.edu.socbot.nlp;

import ua.edu.socbot.dto.IntentScore;
import java.util.List;

public record IntentPrediction(String intent, double confidence, List<IntentScore> topIntents) { }
