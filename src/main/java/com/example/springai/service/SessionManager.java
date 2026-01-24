package com.example.springai.service;

import com.example.springai.model.ConversationSession;
import com.example.springai.model.ConversationSessionDocument;
import com.example.springai.model.MessageDocument;
import com.example.springai.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 대화 세션을 관리하는 서비스
 * MongoDB를 사용하여 대화 히스토리를 영구 저장합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionManager {
    
    private final SessionRepository sessionRepository;
    
    /**
     * 새로운 세션을 생성합니다.
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        ConversationSessionDocument document = new ConversationSessionDocument(sessionId);
        sessionRepository.save(document);
        log.info("🆕 새 세션 생성 (MongoDB): {}", sessionId);
        return sessionId;
    }
    
    /**
     * 세션 ID로 세션을 조회합니다.
     * 세션이 없으면 새로 생성합니다.
     */
    public ConversationSession getOrCreateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = createSession();
        }
        
        final String finalSessionId = sessionId;
        
        ConversationSessionDocument document = sessionRepository.findBySessionId(sessionId)
            .orElseGet(() -> {
                log.info("🆕 세션 없음, 새로 생성 (MongoDB): {}", finalSessionId);
                ConversationSessionDocument newDoc = new ConversationSessionDocument(finalSessionId);
                return sessionRepository.save(newDoc);
            });
        
        // Document → ConversationSession 변환
        return documentToSession(document);
    }
    
    /**
     * 세션을 가져옵니다. (없으면 null 반환)
     */
    public ConversationSession getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
            .map(this::documentToSession)
            .orElse(null);
    }
    
    /**
     * 세션에 메시지를 추가하고 저장합니다.
     */
    public void addMessage(String sessionId, Message message) {
        ConversationSessionDocument document = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다: " + sessionId));
        
        // Message → MessageDocument 변환
        MessageDocument messageDoc = messageToDocument(message);
        document.addMessage(messageDoc);
        
        sessionRepository.save(document);
        log.debug("💾 메시지 저장 (MongoDB): 세션={}, 역할={}", sessionId, messageDoc.getRole());
    }
    
    /**
     * 세션의 대화 히스토리를 초기화합니다.
     */
    public void clearSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(document -> {
            document.clearMessages();
            sessionRepository.save(document);
            log.info("🗑️ 세션 히스토리 초기화 (MongoDB): {}", sessionId);
        });
    }
    
    /**
     * 세션을 삭제합니다.
     */
    public void deleteSession(String sessionId) {
        sessionRepository.deleteBySessionId(sessionId);
        log.info("🗑️ 세션 삭제 (MongoDB): {}", sessionId);
    }
    
    /**
     * 활성 세션 수를 반환합니다.
     */
    public int getActiveSessionCount() {
        long count = sessionRepository.count();
        return (int) count;
    }
    
    /**
     * 만료된 세션을 주기적으로 정리합니다. (5분마다)
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void cleanupExpiredSessions() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(30);
        List<ConversationSessionDocument> expiredSessions = sessionRepository.findExpiredSessions(expiryTime);
        
        if (!expiredSessions.isEmpty()) {
            for (ConversationSessionDocument session : expiredSessions) {
                log.info("🧹 만료된 세션 정리 (MongoDB): {} (마지막 접근: {})", 
                    session.getSessionId(), session.getLastAccessedAt());
                sessionRepository.delete(session);
            }
            log.info("🧹 세션 정리 완료 (MongoDB): {}개 삭제", expiredSessions.size());
        }
    }
    
    /**
     * 모든 세션 정보를 로그로 출력합니다. (디버깅용)
     */
    public void logSessionStats() {
        List<ConversationSessionDocument> allSessions = sessionRepository.findAll();
        log.info("📊 세션 통계 (MongoDB):");
        log.info("  - 활성 세션 수: {}", allSessions.size());
        allSessions.forEach(session -> {
            log.info("  - 세션 {}: 대화 턴 {}, 생성 시간: {}, 마지막 접근: {}", 
                session.getSessionId(), 
                session.getConversationTurnCount(),
                session.getCreatedAt(),
                session.getLastAccessedAt()
            );
        });
    }
    
    // ===== 변환 헬퍼 메서드 =====
    
    /**
     * ConversationSessionDocument를 ConversationSession으로 변환
     */
    private ConversationSession documentToSession(ConversationSessionDocument document) {
        ConversationSession session = new ConversationSession(document.getSessionId());
        
        // MongoDB의 메시지들을 Spring AI Message로 변환
        for (MessageDocument msgDoc : document.getMessages()) {
            Message message = documentToMessage(msgDoc);
            session.addMessage(message);
        }
        
        return session;
    }
    
    /**
     * MessageDocument를 Spring AI Message로 변환
     */
    private Message documentToMessage(MessageDocument msgDoc) {
        String role = msgDoc.getRole();
        String content = msgDoc.getContent();
        
        return switch (role) {
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default -> new UserMessage(content);
        };
    }
    
    /**
     * Spring AI Message를 MessageDocument로 변환
     */
    private MessageDocument messageToDocument(Message message) {
        String role;
        if (message instanceof UserMessage) {
            role = "user";
        } else if (message instanceof AssistantMessage) {
            role = "assistant";
        } else if (message instanceof SystemMessage) {
            role = "system";
        } else {
            role = "user";
        }
        
        return new MessageDocument(role, message.getContent());
    }
}
