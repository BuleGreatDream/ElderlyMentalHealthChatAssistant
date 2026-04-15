package org.demo.learn_langchain4j.Service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.learn_langchain4j.AiService.AiJsonChatClient;
import org.demo.learn_langchain4j.Mapper.AiFactMapper;
import org.demo.learn_langchain4j.Model.AiFactExtractionResult;
import org.demo.learn_langchain4j.Model.AiFactItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiFactExtractionAsyncService {

    private static final Logger log = LoggerFactory.getLogger(AiFactExtractionAsyncService.class);

    private final AiJsonChatClient aiJsonChatClient;
    private final AiFactMapper aiFactMapper;
    private final ObjectMapper objectMapper;

    public AiFactExtractionAsyncService(AiJsonChatClient aiJsonChatClient, AiFactMapper aiFactMapper, ObjectMapper objectMapper) {
        this.aiJsonChatClient = aiJsonChatClient;
        this.aiFactMapper = aiFactMapper;
        this.objectMapper = objectMapper;
    }

    @Async("factExtractionExecutor")
    public void extractAndSave(String memoryId, String userMessage, String finalAnswer) {
        try {
            String extractionInput = "User: " + userMessage + "\nAssistant: " + finalAnswer;
            String raw = aiJsonChatClient.chat(extractionInput);
            String json = normalizeJson(raw);
            log.debug("Fact extraction raw json: {}", json);

            AiFactExtractionResult result = objectMapper.readValue(json, AiFactExtractionResult.class);
            if (result == null || result.facts() == null) {
                log.debug("Fact extraction returned no facts for memoryId={}", memoryId);
                return;
            }

            List<AiFactItem> validFacts = result.facts().stream()
                    .filter(f -> f != null && hasText(f.key()) && hasText(f.value()))
                    .collect(Collectors.toList());

            if (validFacts.isEmpty()) {
                log.debug("Fact extraction returned empty valid facts for memoryId={}", memoryId);
                return;
            }

            aiFactMapper.saveFacts(memoryId, extractionInput, validFacts);
            log.info("Fact extraction persisted {} facts for memoryId={}", validFacts.size(), memoryId);
        } catch (Exception ex) {
            // Extraction failures should not affect the chat response path.
            log.warn("Fact extraction skipped due to parse/persist error for memoryId={}", memoryId, ex);
        }
    }

    private String normalizeJson(String raw) {
        if (raw == null) {
            return "{}";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}


