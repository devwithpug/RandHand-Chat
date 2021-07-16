package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
}
