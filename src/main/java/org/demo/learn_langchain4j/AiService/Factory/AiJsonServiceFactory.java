package org.demo.learn_langchain4j.AiService.Factory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.demo.learn_langchain4j.AiService.AiJsonChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiJsonServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;

    @Bean
    public AiJsonChatClient aiJsonService(){

        return AiServices.builder(AiJsonChatClient.class)
                .chatModel(myQwenChatModel)
                .build();
    }
    
    
}
