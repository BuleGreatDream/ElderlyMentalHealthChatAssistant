package org.demo.learn_langchain4j.AiTool;

import dev.langchain4j.agent.tool.Tool;
import org.demo.learn_langchain4j.Service.CurrentDateTimeService;
import org.springframework.stereotype.Component;

@Component
public class CurrentDateTimeTool {

    private final CurrentDateTimeService currentDateTimeService;

    public CurrentDateTimeTool(CurrentDateTimeService currentDateTimeService) {
        this.currentDateTimeService = currentDateTimeService;
    }

    @Tool(
            name = "CurrentDateTime",
            value = "Get the current local date and time. This tool takes no parameters."
    )
    public String getCurrentDateTime() {
        return currentDateTimeService.getCurrentDateTime();
    }
}

