package org.demo.learn_langchain4j.Controller;

import org.demo.learn_langchain4j.Memory.HelperChatMemoryStore;
import org.demo.learn_langchain4j.AiService.Factory.AiHelperServicFactory;
import org.demo.learn_langchain4j.Service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/chat")
public class AiChatContoller {

    @Autowired
    private AiChatService AiChatServiceImpl;

    @Autowired
    private HelperChatMemoryStore chatMemoryStore;

    @GetMapping("/")
    public String chat(
            @RequestParam("message") String message,
            @RequestParam(value = "medicationTimes", required = false) String medicationTimes
    ) {
        System.out.println("断点");
        return AiChatServiceImpl.chat(message, medicationTimes);
    }

    @DeleteMapping("/memory")
    public String clearMemory() {
        chatMemoryStore.deleteMessages(AiHelperServicFactory.DEFAULT_MEMORY_ID);
        return "Chat memory cleared.";
    }
}
