package org.demo.learn_langchain4j;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "ai.dashscope.api-key=sk-test-only",
        "ai.dashscope.model-name=qwen-plus"
})
class LearnLangChain4jApplicationTests {

    @Test
    void contextLoads() {
    }

}
