package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatEntity, String> {

    @Query("select c from ChatEntity c where ?1 member of c.userIds")
    List<ChatEntity> findAllByUserId(String userId);

    @Query("select distinct c from ChatEntity c join fetch c.userIds where c.sessionId = ?1")
    ChatEntity findChatEntityBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}
