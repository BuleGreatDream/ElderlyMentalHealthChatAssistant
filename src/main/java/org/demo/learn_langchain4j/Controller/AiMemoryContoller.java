package org.demo.learn_langchain4j.Controller;

import org.demo.learn_langchain4j.Tools.ChatRecordExtractor;
import org.demo.learn_langchain4j.Model.AiChatMemoryRecord;
import org.demo.learn_langchain4j.Service.AiMemoryService;
import org.demo.learn_langchain4j.AiService.Factory.AiHelperServicFactory;
import org.demo.learn_langchain4j.Tools.FileTextTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/ai/memory")
public class AiMemoryContoller {

    @Autowired
    AiMemoryService aiMemoryService;

    @GetMapping("/records")
    public List<String> getMemoryRecords(
            @RequestParam(value = "memoryId", defaultValue = AiHelperServicFactory.DEFAULT_MEMORY_ID) String memoryId
    ) {
        List<AiChatMemoryRecord> records = aiMemoryService.getMemoryRecords(memoryId);
        List<String> list = ChatRecordExtractor.extract(records);
        Path savedFile = FileTextTool.writeTxt(Path.of("src/main/resources/Memory"), list);
        System.out.println("=== Memory 已保存到文件: " + savedFile + " ===");
        return list;
    }

}
