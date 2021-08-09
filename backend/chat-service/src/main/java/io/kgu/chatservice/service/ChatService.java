package io.kgu.chatservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.QueueDto;

public interface ChatService {

    QueueDto makeChatQueue(QueueDto queueDto) throws JsonProcessingException;
    ChatDto createChatRoom(ChatDto chatDto);
    ChatDto getOneChatRoomBySessionId(String sessionId);
    ChatDto getOneChatRoomByUserId(String userId);
    void removeChatRoomBySessionId(String sessionId);
}
