package com.example.springai.repository;

import com.example.springai.model.ConversationSessionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 대화 세션 MongoDB Repository
 * Spring Data MongoDB를 사용한 데이터 접근 계층
 */
@Repository
public interface SessionRepository extends MongoRepository<ConversationSessionDocument, String> {
    
    /**
     * 세션 ID로 세션을 조회합니다.
     */
    Optional<ConversationSessionDocument> findBySessionId(String sessionId);
    
    /**
     * 사용자 ID로 모든 세션을 조회합니다.
     */
    List<ConversationSessionDocument> findByUserId(String userId);
    
    /**
     * 마지막 접근 시간이 특정 시간 이전인 세션을 조회합니다.
     * (만료된 세션 정리용)
     */
    @Query("{ 'lastAccessedAt' : { $lt: ?0 } }")
    List<ConversationSessionDocument> findExpiredSessions(LocalDateTime expiryTime);
    
    /**
     * 세션 ID로 세션을 삭제합니다.
     */
    void deleteBySessionId(String sessionId);
    
    /**
     * 사용자 ID로 모든 세션을 삭제합니다.
     */
    void deleteByUserId(String userId);
}
