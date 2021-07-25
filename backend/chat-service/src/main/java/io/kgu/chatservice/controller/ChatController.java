package io.kgu.chatservice.controller;

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

import javax.validation.Valid;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ModelMapper modelMapper;

    @PostMapping("/chats")
    public ResponseEntity<ResponseQueue> makeChatQueue(@Valid @RequestBody RequestQueue requestQueue) {

        chatService.makeChatQueue(modelMapper.map(requestQueue, QueueDto.class));

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseQueue(requestQueue.getUserId()));
    }

    @GetMapping("/chats/{sessionId}")
    public ResponseEntity<ChatDto> getOneChatRoom(@PathVariable String sessionId) {

        ChatDto chatRoom = chatService.getOneChatRoom(sessionId);

        return ResponseEntity.status(HttpStatus.OK).body(chatRoom);
    }

}
