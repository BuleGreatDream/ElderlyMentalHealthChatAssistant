package org.demo.learn_langchain4j.AiService;

import dev.langchain4j.service.SystemMessage;

public interface AiScoreChatClient {

    @SystemMessage(fromResource = "settings/system-score-prompt.txt")
    String chat(String message);

}
