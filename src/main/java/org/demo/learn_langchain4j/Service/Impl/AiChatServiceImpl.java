package org.demo.learn_langchain4j.Service.Impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.demo.learn_langchain4j.AiService.AiHelperDraftChatClient;
import org.demo.learn_langchain4j.AiService.AiScoreChatClient;
import org.demo.learn_langchain4j.AiService.Factory.AiHelperServicFactory;
import org.demo.learn_langchain4j.Memory.HelperChatMemoryStore;
import org.demo.learn_langchain4j.Service.AiChatService;
import org.demo.learn_langchain4j.Service.MedicationScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final int MAX_RETRY = 2;
    private static final int MAX_MEMORY_MESSAGES = 20;

    @Autowired
    private AiHelperDraftChatClient aiHelperDraftChatClient;

    @Autowired
    private AiScoreChatClient aiScoreChatClient;

    @Autowired
    private HelperChatMemoryStore chatMemoryStore;

    @Autowired
    private AiFactExtractionAsyncService aiFactExtractionAsyncService;

    @Autowired
    private MedicationScheduleService medicationScheduleService;


    @Override
    public String chat(String message, String medicationTimes) {
        int retry = 0;
        String promptMessage = medicationScheduleService.buildMedicationAwareMessage(message, medicationTimes);
        String content = aiHelperDraftChatClient.chat(promptMessage);

        while (retry++ < MAX_RETRY && !isCompliant(message, content)) {
            content = aiHelperDraftChatClient.chat(buildRewritePrompt(promptMessage, content));
        }

        persistFinalTurn(message, content);
        aiFactExtractionAsyncService.extractAndSave(AiHelperServicFactory.DEFAULT_MEMORY_ID, message, content);
        return content;
    }

    private boolean isCompliant(String message, String content) {
        String check = aiScoreChatClient.chat(
                "请只返回true或false。用户问题：" + message + "。候选回复：" + content
        );
        return check != null && check.trim().equalsIgnoreCase("true");
    }

    private String buildRewritePrompt(String messageForAi, String content) {
        return "上一个候选回复不符合系统提示词，请基于用户问题重写。"
                + "用户问题：" + messageForAi
                + "。上一版候选回复：" + content
                + "。仅返回新的回复正文，不要解释。";
    }

    private void persistFinalTurn(String message, String content) {
        List<ChatMessage> messages = new ArrayList<>(
                chatMemoryStore.getMessages(AiHelperServicFactory.DEFAULT_MEMORY_ID)
        );
        messages.add(UserMessage.from(message));
        messages.add(AiMessage.from(content));

        // Keep store size aligned with MessageWindowChatMemory maxMessages.
        while (messages.size() > MAX_MEMORY_MESSAGES) {
            messages.remove(0);
        }

        chatMemoryStore.updateMessages(AiHelperServicFactory.DEFAULT_MEMORY_ID, messages);
    }
}
