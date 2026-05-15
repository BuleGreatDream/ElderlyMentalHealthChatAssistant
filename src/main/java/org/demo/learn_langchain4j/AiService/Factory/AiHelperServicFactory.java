package org.demo.learn_langchain4j.AiService.Factory;


import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.demo.learn_langchain4j.AiTool.AutoRecordTool;
import org.demo.learn_langchain4j.AiTool.CurrentDateTimeTool;
//import org.demo.learn_langchain4j.AiTool.ReadNovelTool;
import org.demo.learn_langchain4j.Memory.HelperChatMemoryStore;
import org.demo.learn_langchain4j.AiService.AiHelperChatClient;
import org.demo.learn_langchain4j.AiService.AiHelperDraftChatClient;
import org.demo.learn_langchain4j.AiService.AiHelperStreamingChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiHelperServicFactory {

    public static final String DEFAULT_MEMORY_ID = "ai-helper-default";

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private StreamingChatModel myQwenStreamingChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    @Resource
    private AutoRecordTool autoRecordTool;

//    @Resource
//    private ReadNovelTool readNovelTool;

    @Resource
    private CurrentDateTimeTool currentDateTimeTool;

    @Bean
    public AiHelperChatClient aiHelperService(HelperChatMemoryStore chatMemoryStore){
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(DEFAULT_MEMORY_ID)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();

        AiHelperChatClient aiChatClient = AiServices.builder(AiHelperChatClient.class)
                .chatModel(myQwenChatModel)
                .chatMemory(chatMemory)
                .contentRetriever(contentRetriever)
                .tools(autoRecordTool, currentDateTimeTool)
                .build();

        return aiChatClient;
    }

    @Bean
    public AiHelperDraftChatClient aiHelperDraftService() {
        return AiServices.builder(AiHelperDraftChatClient.class)
                .chatModel(myQwenChatModel)
                .contentRetriever(contentRetriever)
                .tools(autoRecordTool, currentDateTimeTool)
                .build();
    }

    @Bean
    public AiHelperStreamingChatClient aiHelperStreamingService(HelperChatMemoryStore chatMemoryStore) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(DEFAULT_MEMORY_ID)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();

        return AiServices.builder(AiHelperStreamingChatClient.class)
                .streamingChatModel(myQwenStreamingChatModel)
                .chatMemory(chatMemory)
                .contentRetriever(contentRetriever)
                .tools(autoRecordTool, currentDateTimeTool)
                .build();
    }

}
