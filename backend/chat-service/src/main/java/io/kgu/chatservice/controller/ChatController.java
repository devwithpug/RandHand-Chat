package io.kgu.chatservice.controller;

import io.kgu.chatservice.domain.dto.chat.ChatDto;
import io.kgu.chatservice.domain.dto.chat.MessageDto;
import io.kgu.chatservice.domain.dto.chat.MessageSyncDto;
import io.kgu.chatservice.service.ChatService;
import io.kgu.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    @GetMapping("/{sessionId}")
    public ChatDto getOneChatRoom(@PathVariable String sessionId) {

        return chatService.getOneChatRoomBySessionId(sessionId);
    }

    @GetMapping("/session")
    public List<ChatDto> getAllChatRoomByUserId(@RequestHeader("userId") String userId) {

        return chatService.getAllChatRoomByUserId(userId);
    }

    @DeleteMapping("/{sessionId}")
    public void removeChatRoom(@PathVariable("sessionId") String sessionId) {

        chatService.removeChatRoomBySessionId(sessionId);
    }

    @GetMapping("/messages")
    public List<MessageDto> findAllMessages(@RequestHeader("sessionId") String sessionId) {

        ChatDto chatRoom = chatService.getOneChatRoomBySessionId(sessionId);

        return messageService.findAllMessagesByChatRoom(chatRoom);
    }

    @GetMapping("/sync")
    public MessageSyncDto syncChatRoom(
            @RequestHeader("sessionId") String sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {

        ChatDto chatRoom = chatService.getOneChatRoomBySessionId(sessionId);
        List<MessageDto> messages = messageService.syncAllMessagesByChatRoomAndDate(chatRoom, date.plusSeconds(1));

        return new MessageSyncDto(chatRoom.getSyncTime(), messages);
    }
}
