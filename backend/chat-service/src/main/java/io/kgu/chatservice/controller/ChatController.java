package io.kgu.chatservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.QueueDto;
import io.kgu.chatservice.domain.request.RequestQueue;
import io.kgu.chatservice.domain.response.ResponseQueue;
import io.kgu.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ModelMapper mapper;

    @PostMapping("/chats")
    public ResponseEntity<ResponseQueue> makeChatQueue(@Valid @RequestBody RequestQueue requestQueue) {

        QueueDto queueDto;

        try {
            queueDto = chatService.makeChatQueue(mapper.map(requestQueue, QueueDto.class));
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 에러");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(queueDto, ResponseQueue.class));
    }

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
