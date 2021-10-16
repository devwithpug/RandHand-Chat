package io.kgu.chatservice.controller;

import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.MessageDto;
import io.kgu.chatservice.service.ChatService;
import io.kgu.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<ChatDto> getOneChatRoom(@PathVariable String sessionId) {

        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(chatRoom);
    }

    @GetMapping("/session")
    public ResponseEntity<List<ChatDto>> getAllChatRoomByUserId(@RequestHeader("userId") String userId) {

        List<ChatDto> chatRoomList;

        try {
            chatRoomList = chatService.getAllChatRoomByUserId(userId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(chatRoomList);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Object> removeChatRoom(@PathVariable("sessionId") String sessionId) {

        try {
            chatService.removeChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/messages")
    public ResponseEntity<Object> findAllMessages(@RequestHeader("sessionId") String sessionId) {

        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        List<MessageDto> messages = messageService.findAllMessagesByChatRoom(chatRoom);

        return ResponseEntity.status(HttpStatus.OK).body(messages);
    }

    @GetMapping("/sync")
    public ResponseEntity<Object> syncChatRoom(@RequestHeader("sessionId") String sessionId,
                                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime date) {
        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        List<MessageDto> messages = messageService.syncAllMessagesByChatRoomAndDate(chatRoom, date.plusSeconds(1));

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "syncTime", chatRoom.getSyncTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                "message", messages)
        );
    }
}
