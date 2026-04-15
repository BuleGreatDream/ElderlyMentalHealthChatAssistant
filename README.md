# 老年心理健康助手 项目介绍与环境要求

## 1. 项目简介

一个基于 `Spring Boot + LangChain4j` 的对话助手项目，面向“老年心理健康助手”场景，支持：

- 基础多轮对话
- 短期记忆管理（清除/导出）
- RAG 向量检索增强（Chroma）
- 对话后的结构化事实提取（异步）
- 前端录入每日吃药时间，并在接近服药时间时给出提醒上下文

前端为纯 `HTML/CSS/JS`，后端提供 HTTP 接口。

---

## 2. 核心能力

- 对话接口：`GET /ai/chat/`
- 记忆清除：`DELETE /ai/chat/memory`
- 记忆导出：`GET /ai/memory/records`
- 支持通过 `medicationTimes` 参数将服药计划注入到每次对话上下文中

---

## 3. 技术栈

- Java 21
- Spring Boot 3.3.6
- LangChain4j 1.1.0
- 阿里云百炼模型
- MyBatis + MySQL
- Redis
- Chroma 0.6.3
- 前端：HTML + CSS + JavaScript

---

## 4. 环境要求

请确保本地具备以下运行环境：

- JDK 21
- Maven
- MySQL 8.x
- Redis
- Chroma 0.6.3 及以上 (支持V2)
- 阿里云百炼 API Key

---

## 5. 配置说明

项目使用 `local` profile：

- 公共配置：`src/main/resources/application.yml`


```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_lc_memory?useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: <your_password>

langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: <your_dashscope_key>
      embedding-model:
        api-key: <your_dashscope_key>
```

---

## 6. 数据库初始化

项目已配置启动时执行 SQL 初始化：

- SQL 文件：`src/main/resources/sql/ai_fact_memory.sql`
- 默认会创建数据库与相关表（如 `ai_chat_memory`、`ai_fact_memory`）

请确保 MySQL 用户有建库建表权限。

---

## 7. 启动步骤

在项目根目录执行：

```powershell
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd spring-boot:run
```

启动后访问：

- 前端页面：`http://localhost:8080/`
---
