package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    ChatEntity findBySessionId(String sessionId);

    @Query("select c from ChatEntity c where ?1 member of c.userIds")
    ChatEntity findByUserId(String userId);

    void deleteBySessionId(String sessionId);
}
