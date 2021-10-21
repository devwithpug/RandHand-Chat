package io.kgu.chatservice.service;

import io.kgu.chatservice.domain.dto.chat.ChatDto;
import io.kgu.chatservice.domain.dto.chat.MessageDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import org.springframework.web.socket.AbstractWebSocketMessage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {

    MessageDto create(AbstractWebSocketMessage<?> message, ChatEntity chat, String from) throws IOException;
    List<MessageDto> findAllMessagesByChatRoom(ChatDto chatDto);
    List<MessageDto> syncAllMessagesByChatRoomAndDate(ChatDto chatDto, LocalDateTime date);

}
