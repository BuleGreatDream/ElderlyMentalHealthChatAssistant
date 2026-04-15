package org.demo.learn_langchain4j.Mapper;

import org.demo.learn_langchain4j.Model.AiFactItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;

@Repository
public class AiFactMapper {

    private final JdbcTemplate jdbcTemplate;

    public AiFactMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveFacts(String memoryId, String sourceMessage, List<AiFactItem> facts) {
        if (facts == null || facts.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO ai_fact_memory(memory_id, fact_key, fact_value, confidence, source_message) VALUES (?, ?, ?, ?, ?)",
                facts,
                facts.size(),
                (ps, fact) -> {
                    ps.setString(1, memoryId);
                    ps.setString(2, fact.key());
                    ps.setString(3, fact.value());
                    if (fact.confidence() == null) {
                        ps.setNull(4, Types.DECIMAL);
                    } else {
                        ps.setBigDecimal(4, BigDecimal.valueOf(fact.confidence()));
                    }
                    ps.setString(5, sourceMessage);
                }
        );
    }
}


