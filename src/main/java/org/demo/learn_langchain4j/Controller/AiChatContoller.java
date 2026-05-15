package org.demo.learn_langchain4j.Controller;

import dev.langchain4j.service.TokenStream;
import org.demo.learn_langchain4j.Memory.HelperChatMemoryStore;
import org.demo.learn_langchain4j.AiService.Factory.AiHelperServicFactory;
import org.demo.learn_langchain4j.Service.AiChatService;
import org.demo.learn_langchain4j.Service.Impl.AiFactExtractionAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/ai/chat")
public class AiChatContoller {

    @Autowired
    private AiChatService AiChatServiceImpl;

    @Autowired
    private HelperChatMemoryStore chatMemoryStore;

    @Autowired
    private AiFactExtractionAsyncService aiFactExtractionAsyncService;

    @GetMapping("/")
    public String chat(
            @RequestParam("message") String message,
            @RequestParam(value = "medicationTimes", required = false) String medicationTimes
    ) {
        System.out.println("断点");
        return AiChatServiceImpl.chat(message, medicationTimes);
    }

    @GetMapping("/stream")
    public SseEmitter chatStream(
            @RequestParam("message") String message,
            @RequestParam(value = "medicationTimes", required = false) String medicationTimes
    ) {
        SseEmitter emitter = new SseEmitter(300_000L);

        TokenStream tokenStream = AiChatServiceImpl.chatStream(message, medicationTimes);
        StringBuilder fullResponse = new StringBuilder();

        tokenStream
                .onPartialResponse(token -> {
                    try {
                        emitter.send(SseEmitter.event().data(token));
                        fullResponse.append(token);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> {
                    aiFactExtractionAsyncService.extractAndSave(
                            AiHelperServicFactory.DEFAULT_MEMORY_ID,
                            message,
                            fullResponse.toString()
                    );
                    emitter.complete();
                })
                .onError(emitter::completeWithError)
                .start();

        return emitter;
    }

    @DeleteMapping("/memory")
    public String clearMemory() {
        chatMemoryStore.deleteMessages(AiHelperServicFactory.DEFAULT_MEMORY_ID);
        return "Chat memory cleared.";
    }
}
