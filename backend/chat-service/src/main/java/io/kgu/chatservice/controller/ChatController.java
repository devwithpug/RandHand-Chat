package io.kgu.chatservice.controller;

import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chats/{sessionId}")
    public ResponseEntity<ChatDto> getOneChatRoom(@PathVariable String sessionId) {

        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomBySessionId(sessionId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(chatRoom);
    }

    @GetMapping("/chats/session")
    public ResponseEntity<ChatDto> getOneChatRoomByUserId(@RequestHeader("userId") String userId) {

        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomByUserId(userId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(chatRoom);
    }

    @GetMapping("/chats/leave")
    public ResponseEntity<Object> removeChatRoom(@RequestHeader("userId") String userId) {

        ChatDto chatRoom;

        try {
            chatRoom = chatService.getOneChatRoomByUserId(userId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        chatService.removeChatRoomBySessionId(chatRoom.getSessionId());

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
