package org.demo.learn_langchain4j.Tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.learn_langchain4j.Model.AiChatMemoryRecord;

import java.util.ArrayList;
import java.util.List;

public class ChatRecordExtractor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从记录列表中提取 AI 和 USER 消息，格式化为 "日期：发送者:内容"
     *
     * @param records 原始记录列表
     * @return 格式化后的字符串列表
     */
    public static List<String> extract(List<AiChatMemoryRecord> records) {
        List<String> result = new ArrayList<>();
        for (AiChatMemoryRecord record : records) {
            try {
                JsonNode root = objectMapper.readTree(record.messageJson());
                String type = root.path("type").asText();
                if (!"AI".equals(type) && !"USER".equals(type)) {
                    continue; // 忽略 SYSTEM 等其他类型
                }

                String rawText = extractRawText(root, type);
                if (rawText == null || rawText.isBlank()) {
                    continue;
                }

                // 对 USER 消息进行清理：只保留第一个换行符之前的内容（去除 "Answer using..." 等附加信息）
                if ("USER".equals(type)) {
                    int breakIndex = rawText.indexOf("\n\n");
                    if (breakIndex != -1) {
                        rawText = rawText.substring(0, breakIndex);
                    }
                }

                // 将文本中的换行符替换为空格，保证输出为单行
                String cleanedText = rawText.replaceAll("\\s+", " ").trim();

                String date = extractDate(record.updatedAt());
                String sender = "USER".equals(type) ? "bluedream" : "dorothyhaze";

                String formatted = String.format("%s：%s:%s", date, sender, cleanedText);
                result.add(formatted);
            } catch (Exception e) {
                System.err.println("解析消息失败: " + e.getMessage());
            }
        }
        return result;
    }

    private static String extractRawText(JsonNode root, String type) {
        if ("AI".equals(type)) {
            return root.path("text").asText();
        } else if ("USER".equals(type)) {
            JsonNode contents = root.path("contents");
            if (contents.isArray() && contents.size() > 0) {
                JsonNode firstContent = contents.get(0);
                return firstContent.path("text").asText();
            }
        }
        return null;
    }

    private static String extractDate(String updatedAt) {
        if (updatedAt == null || updatedAt.length() < 10) {
            return "未知日期";
        }
        return updatedAt.substring(0, 10); // 取 "yyyy-MM-dd"
    }

}