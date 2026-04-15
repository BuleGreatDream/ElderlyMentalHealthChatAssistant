package org.demo.learn_langchain4j.AiService;

import dev.langchain4j.service.SystemMessage;

public interface AiJsonChatClient {

    @SystemMessage(fromResource = "settings/system-json-prompt.txt")
    String chat(String message);

}
