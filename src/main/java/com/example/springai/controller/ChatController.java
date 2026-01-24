package com.example.springai.controller;

import com.example.springai.dto.ChatRequest;
import com.example.springai.dto.ChatResponse;
import com.example.springai.model.ConversationSession;
import com.example.springai.service.ChatService;
import com.example.springai.service.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Chat API Controller
 * 대화 세션을 관리하고 AI 채팅 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SessionManager sessionManager;

    /**
     * 채팅 메시지 전송
     */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("📨 Chat API 호출: {} (세션: {})", request.message(), request.sessionId());
        return chatService.chat(request.message(), request.sessionId());
    }

    /**
     * 새로운 세션 생성
     */
    @PostMapping("/session/new")
    public Map<String, String> createNewSession() {
        String sessionId = sessionManager.createSession();
        log.info("🆕 새 세션 생성 요청: {}", sessionId);
        return Map.of("sessionId", sessionId);
    }

    /**
     * 세션 히스토리 초기화
     */
    @DeleteMapping("/session/{sessionId}/clear")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        sessionManager.clearSession(sessionId);
        log.info("🗑️ 세션 히스토리 초기화: {}", sessionId);
        return Map.of("message", "세션 히스토리가 초기화되었습니다.", "sessionId", sessionId);
    }

    /**
     * 세션 삭제
     */
    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> deleteSession(@PathVariable String sessionId) {
        sessionManager.deleteSession(sessionId);
        log.info("🗑️ 세션 삭제: {}", sessionId);
        return Map.of("message", "세션이 삭제되었습니다.");
    }

    /**
     * 세션 정보 조회
     */
    @GetMapping("/session/{sessionId}")
    public Map<String, Object> getSessionInfo(@PathVariable String sessionId) {
        ConversationSession session = sessionManager.getSession(sessionId);
        if (session == null) {
            return Map.of("error", "세션을 찾을 수 없습니다.");
        }
        
        return Map.of(
            "sessionId", session.getSessionId(),
            "conversationTurn", session.getConversationTurnCount(),
            "createdAt", session.getCreatedAt().toString(),
            "lastAccessedAt", session.getLastAccessedAt().toString()
        );
    }

    /**
     * 활성 세션 수 조회
     */
    @GetMapping("/sessions/count")
    public Map<String, Integer> getActiveSessionCount() {
        int count = sessionManager.getActiveSessionCount();
        log.info("📊 활성 세션 수: {}", count);
        return Map.of("activeSessionCount", count);
    }

    /**
     * 헬스체크
     */
    @GetMapping("/test")
    public String test() {
        return "Spring AI Function-Calling is working! 🚀";
    }
}

