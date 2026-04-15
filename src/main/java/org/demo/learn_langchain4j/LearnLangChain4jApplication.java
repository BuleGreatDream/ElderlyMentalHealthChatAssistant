package org.demo.learn_langchain4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LearnLangChain4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearnLangChain4jApplication.class, args);
    }

}
