package com.example.springai.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB에 저장되는 대화 세션 Document
 * 사용자와의 전체 대화 히스토리를 영구 저장합니다.
 */
@Data
@NoArgsConstructor
@Document(collection = "conversation_sessions")
public class ConversationSessionDocument {
    
    /**
     * 세션 고유 ID
     */
    @Id
    private String sessionId;
    
    /**
     * 사용자 ID (선택사항)
     */
    private String userId;
    
    /**
     * 대화 메시지 리스트
     */
    private List<MessageDocument> messages = new ArrayList<>();
    
    /**
     * 세션 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 마지막 접근 시간
     */
    private LocalDateTime lastAccessedAt;
    
    /**
     * 세션 메타데이터 (선택사항)
     */
    private String metadata;
    
    public ConversationSessionDocument(String sessionId) {
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        this.messages = new ArrayList<>();
    }
    
    /**
     * 메시지를 추가하고 마지막 접근 시간을 업데이트합니다.
     */
    public void addMessage(MessageDocument message) {
        this.messages.add(message);
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * 대화 턴 수를 반환합니다.
     */
    public int getConversationTurnCount() {
        return messages.size();
    }
    
    /**
     * 대화 히스토리를 초기화합니다.
     */
    public void clearMessages() {
        this.messages.clear();
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * 세션이 만료되었는지 확인 (30분 이상 비활성)
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(lastAccessedAt.plusMinutes(30));
    }
    
    /**
     * 마지막 접근 시간을 업데이트합니다.
     */
    public void updateLastAccessedAt() {
        this.lastAccessedAt = LocalDateTime.now();
    }
}
