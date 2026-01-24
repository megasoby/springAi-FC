package com.example.springai.dto;

/**
 * 채팅 응답 DTO
 * 
 * @param response AI 응답 메시지
 * @param functionCalled Function이 호출되었는지 여부
 * @param sessionId 세션 ID
 * @param conversationTurn 현재 대화 턴 수
 */
public record ChatResponse(
        String response,
        boolean functionCalled,
        String sessionId,
        int conversationTurn
) {
    // 하위 호환성을 위한 생성자
    public ChatResponse(String response, boolean functionCalled) {
        this(response, functionCalled, null, 0);
    }
}

