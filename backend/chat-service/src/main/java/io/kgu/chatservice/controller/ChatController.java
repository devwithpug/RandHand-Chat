package io.kgu.chatservice.controller;

import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.MessageDto;
import io.kgu.chatservice.domain.dto.MessageSyncDto;
import io.kgu.chatservice.service.ChatService;
import io.kgu.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
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

        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return chatRoom;
    }

    @GetMapping("/session")
    public List<ChatDto> getAllChatRoomByUserId(@RequestHeader("userId") String userId) {

        List<ChatDto> chatRoomList;

        try {
            chatRoomList = chatService.getAllChatRoomByUserId(userId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return chatRoomList;
    }

    @DeleteMapping("/{sessionId}")
    public void removeChatRoom(@PathVariable("sessionId") String sessionId) {

        try {
            chatService.removeChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/messages")
    public List<MessageDto> findAllMessages(@RequestHeader("sessionId") String sessionId) {

        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return messageService.findAllMessagesByChatRoom(chatRoom);
    }

    @GetMapping("/sync")
    public MessageSyncDto syncChatRoom(
            @RequestHeader("sessionId") String sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        List<MessageDto> messages = messageService.syncAllMessagesByChatRoomAndDate(chatRoom, date.plusSeconds(1));

        return new MessageSyncDto(chatRoom.getSyncTime(), messages);
    }
}
