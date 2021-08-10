package io.kgu.chatservice.service;

import io.kgu.chatservice.domain.dto.ChatDto;

public interface ChatService {

    ChatDto createChatRoom(ChatDto chatDto);
    ChatDto getOneChatRoomBySessionId(String sessionId);
    ChatDto getOneChatRoomByUserId(String userId);
    void removeChatRoomBySessionId(String sessionId);

}
