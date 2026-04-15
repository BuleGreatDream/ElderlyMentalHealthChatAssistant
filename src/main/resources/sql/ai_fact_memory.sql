CREATE DATABASE IF NOT EXISTS ai_lc_memory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_lc_memory;

CREATE TABLE IF NOT EXISTS ai_fact_memory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    memory_id VARCHAR(128) NOT NULL,
    fact_key VARCHAR(128) NOT NULL,
    fact_value TEXT NOT NULL,
    confidence DECIMAL(4,3) NULL,
    source_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_fact_memory_memory_id (memory_id),
    INDEX idx_ai_fact_memory_key (fact_key)
);

CREATE TABLE IF NOT EXISTS ai_chat_memory (
    memory_id VARCHAR(128) NOT NULL,
    seq INT NOT NULL,
    message_json LONGTEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (memory_id, seq)
);

