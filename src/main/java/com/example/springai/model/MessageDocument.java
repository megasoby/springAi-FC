package com.example.springai.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 개별 메시지를 저장하는 Document 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDocument {
    
    /**
     * 메시지 역할 (user, assistant, system)
     */
    private String role;
    
    /**
     * 메시지 내용
     */
    private String content;
    
    /**
     * 메시지 생성 시간
     */
    private LocalDateTime timestamp;
    
    /**
     * Function이 호출되었는지 여부
     */
    private Boolean functionCalled;
    
    public MessageDocument(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.functionCalled = false;
    }
}
