package org.demo.learn_langchain4j.AiTool;

import dev.langchain4j.agent.tool.Tool;
import org.demo.learn_langchain4j.Model.AiChatMemoryRecord;
import org.demo.learn_langchain4j.Service.AiMemoryService;
import org.demo.learn_langchain4j.Tools.ChatRecordExtractor;
import org.demo.learn_langchain4j.Tools.FileTextTool;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class AutoRecordTool {

    private final AiMemoryService aiMemoryService;

    public AutoRecordTool(AiMemoryService aiMemoryService) {
        this.aiMemoryService = aiMemoryService;
    }

    @Tool(name="MemoryRecord",
            value="A memory retention tool that persistently saves conversation history. Accepts an ai_name parameter to identify the AI participant. Invoke this tool when the user requests to save the conversation memory, ends the conversation, or when the AI determines that the current dialogue is complete and worth retaining.")
    public String record(String AiName){
        String memoryId = "";
        if (AiName.compareTo("DorothyHaze") == 0 || AiName.compareTo("Dorothy") == 0){
            memoryId = "ai-helper-default";
        }else{
            return "Only DorothyHaze can use this tool.";
        }
        List<AiChatMemoryRecord> records = aiMemoryService.getMemoryRecords(memoryId);
        List<String> list = ChatRecordExtractor.extract(records);
        FileTextTool.writeTxt(Path.of("src/main/resources/Memory"), list);
        return "Memory saved successfully.";
    }

}
