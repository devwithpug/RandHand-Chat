package io.kgu.chatservice.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.chatservice.domain.dto.chat.ChatDto;
import io.kgu.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = {"match.queue"})
    public void receiveMessage(final Message message) {
        log.info(message.toString());
        try {
            ChatDto chatDto = objectMapper.readValue(message.getBody(), ChatDto.class);
            chatService.createChatRoom(chatDto);
            log.info(chatDto.getUserIds().toString());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
