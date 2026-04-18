package org.demo.learn_langchain4j.AiTool;

import org.demo.learn_langchain4j.Service.AiMemoryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoRecordToolTest {

    @Test
    void recordShouldRejectNonDorothyNames() {
        AiMemoryService aiMemoryService = memoryId -> java.util.List.of();
        AutoRecordTool autoRecordTool = new AutoRecordTool(aiMemoryService);

        String result = autoRecordTool.record("OtherAi");

        assertEquals("Only DorothyHaze can use this tool.", result);
    }
}

