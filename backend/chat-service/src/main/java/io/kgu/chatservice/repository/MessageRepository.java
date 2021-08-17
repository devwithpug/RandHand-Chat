package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.domain.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findAllByChat(ChatEntity chat);
    List<MessageEntity> findAllByChatAndCreatedAtAfter(ChatEntity chat, LocalDateTime date);
    int deleteAllByCreatedAtBefore(LocalDateTime date);
}
