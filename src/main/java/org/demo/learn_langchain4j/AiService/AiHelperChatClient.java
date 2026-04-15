package org.demo.learn_langchain4j.AiService;

import dev.langchain4j.service.SystemMessage;

public interface AiHelperChatClient {

    @SystemMessage(fromResource = "settings/system-prompt.txt")
    String chat(String message);


}

