package io.kgu.chatservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.QueueDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final Environment env;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendQueue(QueueDto queueDto) {

        String jsonInString = "";

        try {
            jsonInString = objectMapper.writeValueAsString(queueDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaTemplate.send(env.getProperty("kafka.topic.queue"), jsonInString);
        log.info("Kafka Producer sent data to queue topic: " + queueDto);
    }

    public void sendMatchedInfo(ChatDto chatDto) {

        String jsonInString = "";

        try {
            jsonInString = objectMapper.writeValueAsString(chatDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaTemplate.send(env.getProperty("kafka.topic.chat"), jsonInString);
        log.info("Kafka Producer sent data to chat topic: " + chatDto);

    }

}
