package org.demo.learn_langchain4j.Service;

import dev.langchain4j.service.TokenStream;

public interface AiChatService {

    String chat(String message, String medicationTimes);

    TokenStream chatStream(String message, String medicationTimes);
}
