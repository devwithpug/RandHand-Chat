package io.kgu.chatservice.service;

import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.QueueDto;

public interface ChatService {

    QueueDto makeChatQueue(QueueDto queueDto);
    ChatDto createChatRoom(ChatDto chatDto);
    ChatDto getOneChatRoom(String sessionId);

}
