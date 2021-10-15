package io.kgu.chatservice.controller;

import com.amazonaws.AmazonServiceException;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.MessageDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.domain.entity.MessageContentType;
import io.kgu.chatservice.domain.entity.MessageEntity;
import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.repository.MessageRepository;
import io.kgu.chatservice.service.AmazonS3Service;
import io.kgu.chatservice.service.ChatService;
import io.kgu.chatservice.service.FirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class TestController {

    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final ModelMapper mapper;
    private final AmazonS3Service amazonS3Service;
    private final MessageRepository messageRepository;
    private final FirebaseMessagingService firebaseMessagingService;

    @GetMapping("/chats/test")
    public String getForm() {
        return "basic/main";
    }

    @PostMapping("/chats/test")
    public String makeChatQueueTest(
            @RequestParam("user") String userId,
            @RequestParam("file") MultipartFile image
    ) {

        try {
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());

//            RequestQueue queue = new RequestQueue();
//            queue.setGesture(base64);
//            queue.setUserId(userId);

//            chatService.makeChatQueue(mapper.map(queue, QueueDto.class));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/chats/test";
    }


    @PostMapping("/s3/upload")
    @ResponseBody
    public ResponseEntity<Object> s3UploadByUrlTest(@RequestParam String requestUrl, @RequestParam String key) {

        String url;

        try {
            url = amazonS3Service.upload(requestUrl, key);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(url);
    }

    @PostMapping("/s3/test")
    @ResponseBody
    public ResponseEntity<Object> s3UploadTest(@RequestParam MultipartFile image) {

        String url;

        try {
            url = amazonS3Service.upload(image, "test");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(url);
    }

    @GetMapping("/s3/delete")
    @ResponseBody
    public ResponseEntity<Object> s3DeleteTest() {

        try {
            amazonS3Service.delete("test");
        } catch (AmazonServiceException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getErrorMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/test/chat/create")
    public void createChatRoom(@RequestHeader String user1, @RequestHeader String user2) {

        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setSessionId(UUID.randomUUID().toString());
        chatEntity.setUserIds(List.of(user1, user2));
        chatEntity.setSyncTime(LocalDateTime.now());
        chatEntity.setMessages(new ArrayList<>());

        ChatEntity result = chatRepository.save(chatEntity);

        log.info(result.toString());
    }

    @GetMapping("/test/chat/info")
    public void getChatInfo(@RequestHeader String sessionId) {

        ChatEntity bySessionId = chatRepository.findById(sessionId).get();

        log.info(bySessionId.toString());
    }

    @GetMapping("/test/msg/after")
    public ResponseEntity<Object> getAfterTest(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime date,
                                               @RequestParam String sessionId) {

        ChatEntity chat = chatRepository.findChatEntityBySessionId(sessionId);

        List<MessageEntity> result = messageRepository.findAllByChatAndCreatedAtAfter(chat, date);

        List<MessageDto> collect = result.stream()
                .map(e -> mapper.map(e, MessageDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("syncTime", chat.getSyncTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), "message", collect));
    }

    @PostMapping("/test/push/{token}")
    public ResponseEntity<Object> pushMessageTest(@PathVariable String token, @RequestParam String body, @RequestParam String from) {

        MessageDto message = new MessageDto();
        message.setContent(body);
        message.setType(MessageContentType.TEXT);
        message.setCreatedAt(LocalDateTime.now());
        message.setFromUser(from);

        try {
            firebaseMessagingService.sendNotification(message, token);
        } catch (FirebaseMessagingException e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
