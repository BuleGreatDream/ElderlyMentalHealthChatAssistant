package org.demo.learn_langchain4j.Mapper;

import org.demo.learn_langchain4j.Model.AiChatMemoryRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class AiMapper {

	private final JdbcTemplate jdbcTemplate;

	public AiMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<AiChatMemoryRecord> findMemoryRecords(String memoryId) {
		return jdbcTemplate.query(
				"SELECT message_json, updated_at FROM ai_chat_memory WHERE memory_id = ? ORDER BY seq ASC",
				(rs, rowNum) -> new AiChatMemoryRecord(
						rs.getString("message_json"),
						toTimestamp(rs.getTimestamp("updated_at"))
				),
				memoryId
		);
	}

	private String toTimestamp(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime().toString();
	}
}
