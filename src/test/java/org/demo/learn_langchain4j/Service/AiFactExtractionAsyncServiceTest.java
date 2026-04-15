package org.demo.learn_langchain4j.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.learn_langchain4j.AiService.AiJsonChatClient;
import org.demo.learn_langchain4j.Mapper.AiFactMapper;
import org.demo.learn_langchain4j.Service.Impl.AiFactExtractionAsyncService;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiFactExtractionAsyncServiceTest {

    @Test
    void shouldParseAndPersistFacts() {
        AiJsonChatClient aiJsonChatClient = mock(AiJsonChatClient.class);
        AiFactMapper aiFactMapper = mock(AiFactMapper.class);
        AiFactExtractionAsyncService service = new AiFactExtractionAsyncService(aiJsonChatClient, aiFactMapper, new ObjectMapper());

        when(aiJsonChatClient.chat("User: 你好\nAssistant: 你好呀"))
                .thenReturn("{\"facts\":[{\"key\":\"city\",\"value\":\"Shanghai\",\"confidence\":0.9}]}");

        service.extractAndSave("ai-helper-default", "你好", "你好呀");

        verify(aiFactMapper).saveFacts(
                org.mockito.ArgumentMatchers.eq("ai-helper-default"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.argThat(facts -> facts != null && facts.size() == 1)
        );
    }

    @Test
    void shouldSkipPersistWhenJsonInvalid() {
        AiJsonChatClient aiJsonChatClient = mock(AiJsonChatClient.class);
        AiFactMapper aiFactMapper = mock(AiFactMapper.class);
        AiFactExtractionAsyncService service = new AiFactExtractionAsyncService(aiJsonChatClient, aiFactMapper, new ObjectMapper());

        when(aiJsonChatClient.chat("User: hi\nAssistant: hello")).thenReturn("not-json");

        service.extractAndSave("ai-helper-default", "hi", "hello");

        verify(aiFactMapper, never()).saveFacts(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyList());
    }
}


