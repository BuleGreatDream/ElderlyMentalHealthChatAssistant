package org.demo.learn_langchain4j.Service;

import jakarta.annotation.Resource;
import org.demo.learn_langchain4j.AiService.AiHelperChatClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiHealthHelperServiceTest {

    @Resource
    private AiHelperChatClient aiHealthHelperService;

    @Test
    void chat() {
        String chat = aiHealthHelperService.chat("");
        System.out.println(chat);
    }

}