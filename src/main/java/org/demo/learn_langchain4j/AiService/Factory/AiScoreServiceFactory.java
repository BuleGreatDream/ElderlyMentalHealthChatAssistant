package org.demo.learn_langchain4j.AiService.Factory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.demo.learn_langchain4j.AiService.AiScoreChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiScoreServiceFactory {

    public static final String DEFAULT_MEMORY_ID = "ai-score-default";

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    @Bean
    public AiScoreChatClient aiScoreService(){

        return AiServices.builder(AiScoreChatClient.class)
                .chatModel(myQwenChatModel)
                .contentRetriever(contentRetriever)
                .build();
    }

}
