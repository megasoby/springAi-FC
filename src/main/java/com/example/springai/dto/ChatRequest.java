package com.example.springai.dto;

/**
 * 채팅 요청 DTO
 * 
 * @param message 사용자 메시지
 * @param sessionId 세션 ID (선택사항, 없으면 새 세션 생성)
 */
public record ChatRequest(
        String message,
        String sessionId
) {
    // 메시지만 있는 생성자 (하위 호환성)
    public ChatRequest(String message) {
        this(message, null);
    }
}

