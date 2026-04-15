package org.demo.learn_langchain4j.Memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HelperChatMemoryStore implements ChatMemoryStore {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return jdbcTemplate.query(
                "SELECT message_json FROM ai_chat_memory WHERE memory_id = ? ORDER BY seq ASC",
                (resultSet, rowNum) -> ChatMessageDeserializer.messageFromJson(resultSet.getString("message_json")),
                String.valueOf(memoryId)
        );
    }

    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String id = String.valueOf(memoryId);
        jdbcTemplate.update("DELETE FROM ai_chat_memory WHERE memory_id = ?", id);

        for (int i = 0; i < messages.size(); i++) {
            jdbcTemplate.update(
                    "INSERT INTO ai_chat_memory(memory_id, seq, message_json) VALUES (?, ?, ?)",
                    id,
                    i,
                    ChatMessageSerializer.messageToJson(messages.get(i))
            );
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        jdbcTemplate.update("DELETE FROM ai_chat_memory WHERE memory_id = ?", String.valueOf(memoryId));
    }
}
