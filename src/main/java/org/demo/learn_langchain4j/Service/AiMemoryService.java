package org.demo.learn_langchain4j.Service;

import org.demo.learn_langchain4j.Model.AiChatMemoryRecord;

import java.util.List;

public interface AiMemoryService {

	List<AiChatMemoryRecord> getMemoryRecords(String memoryId);
}
