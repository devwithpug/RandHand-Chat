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

        QueueDto queueDto = chatService.makeChatQueue(modelMapper.map(requestQueue, QueueDto.class));

        if (queueDto == null) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseQueue("매칭 요청 실패 : userId, gesture 값이 유효하지 않습니다."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(queueDto, ResponseQueue.class));
    }

    @GetMapping("/chats/{sessionId}")
    public ResponseEntity<ChatDto> getOneChatRoom(@PathVariable String sessionId) {

        ChatDto chatRoom = chatService.getOneChatRoom(sessionId);

        if (chatRoom == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ChatDto.errorResponseDetails("웹소켓 세션 조회 실패 : 존재하지 않는 세션입니다. " + sessionId));
        }

        return ResponseEntity.status(HttpStatus.OK).body(chatRoom);
    }

}
