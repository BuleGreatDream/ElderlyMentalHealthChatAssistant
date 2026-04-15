package org.demo.learn_langchain4j.Service.Impl;

import org.demo.learn_langchain4j.Mapper.AiMapper;
import org.demo.learn_langchain4j.Model.AiChatMemoryRecord;
import org.demo.learn_langchain4j.Service.AiMemoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiMemoryServiceImpl implements AiMemoryService {

	private final AiMapper aiMapper;

	public AiMemoryServiceImpl(AiMapper aiMapper) {
		this.aiMapper = aiMapper;
	}

	@Override
	public List<AiChatMemoryRecord> getMemoryRecords(String memoryId) {
		return aiMapper.findMemoryRecords(memoryId);
	}

}
