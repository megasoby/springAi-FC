package com.example.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring AI Function-Calling 애플리케이션
 * 대화 기억 기능을 포함한 AI 챗봇 서비스
 */
@SpringBootApplication
@EnableScheduling
public class SpringAiFunctionCallingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiFunctionCallingApplication.class, args);
    }
}

