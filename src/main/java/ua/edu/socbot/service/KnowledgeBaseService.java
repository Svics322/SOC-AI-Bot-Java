package ua.edu.socbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ua.edu.socbot.model.KnowledgeItem;
import java.io.InputStream;
import java.util.*;

@Service
public class KnowledgeBaseService {
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, KnowledgeItem> knowledge = new HashMap<>();
    @PostConstruct public void init() {
        try (InputStream in = new ClassPathResource("data/knowledge_base.json").getInputStream()) {
            knowledge = mapper.readValue(in, new TypeReference<>() {});
        } catch (Exception e) { throw new IllegalStateException("Не вдалося завантажити базу знань", e); }
    }
    public KnowledgeItem findByIntent(String intent) { return knowledge.getOrDefault(intent, knowledge.get("unknown")); }
}
