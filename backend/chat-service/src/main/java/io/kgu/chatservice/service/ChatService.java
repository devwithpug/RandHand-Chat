package io.kgu.chatservice.service;

import io.kgu.chatservice.domain.dto.chat.ChatDto;

import java.util.List;

public interface ChatService {

    ChatDto createChatRoom(ChatDto chatDto);
    ChatDto getOneChatRoomBySessionId(String sessionId);
    List<ChatDto> getAllChatRoomByUserId(String userId);
    void removeChatRoomBySessionId(String sessionId);

}
