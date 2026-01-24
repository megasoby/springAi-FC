package com.example.springai.model;

import org.springframework.ai.chat.messages.Message;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 대화 세션 정보를 저장하는 클래스
 * 사용자와의 대화 히스토리를 관리합니다.
 */
public class ConversationSession {
    
    private final String sessionId;
    private final List<Message> messageHistory;
    private final LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    
    public ConversationSession(String sessionId) {
        this.sessionId = sessionId;
        this.messageHistory = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * 메시지를 히스토리에 추가합니다.
     */
    public void addMessage(Message message) {
        this.messageHistory.add(message);
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * 모든 메시지 히스토리를 반환합니다.
     */
    public List<Message> getMessageHistory() {
        this.lastAccessedAt = LocalDateTime.now();
        return new ArrayList<>(messageHistory);
    }
    
    /**
     * 대화 히스토리를 초기화합니다.
     */
    public void clearHistory() {
        this.messageHistory.clear();
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * 대화 턴 수를 반환합니다.
     */
    public int getConversationTurnCount() {
        return messageHistory.size();
    }
    
    // Getters
    public String getSessionId() {
        return sessionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    /**
     * 세션이 오래되었는지 확인 (30분 이상 비활성)
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(lastAccessedAt.plusMinutes(30));
    }
}
