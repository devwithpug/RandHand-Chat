package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    ChatEntity findBySessionId(String sessionId);

}
