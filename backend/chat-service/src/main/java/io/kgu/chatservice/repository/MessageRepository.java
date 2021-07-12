package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
