package io.kgu.chatservice.messagequeue;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final KafkaProducer kafkaProducer;

    @KafkaListener(topics = "${kafka.topic.match}")
    public void handleMatchingQueue(String kafkaMessage) throws JsonProcessingException {

        log.info("kafkaMessage: " + kafkaMessage);

        ChatDto chatDto = objectMapper.readValue(kafkaMessage, ChatDto.class);

        chatDto = chatService.createChatRoom(chatDto);
        kafkaProducer.sendMatchedInfo(chatDto);

    }

}
