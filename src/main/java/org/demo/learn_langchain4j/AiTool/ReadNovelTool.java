//package org.demo.learn_langchain4j.AiTool;
//
//import dev.langchain4j.agent.tool.Tool;
//import org.demo.learn_langchain4j.Service.ReadNovelService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ReadNovelTool {
//
//    @Autowired
//    private ReadNovelService ReadNovelService;
//
//
//    @Tool(name = "ReadDorothySettings",
//            value = "Read the character settings for either DorothyHaze or Dorothy. This tool requires no input parameters and returns the settings text directly upon invocation.")
//    public String read() {
//        return ReadNovelService.read();
//    }
//}
