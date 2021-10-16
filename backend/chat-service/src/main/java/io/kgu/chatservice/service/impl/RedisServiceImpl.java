package io.kgu.chatservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.chatservice.service.RedisService;
import io.kgu.chatservice.messaging.custom.WebSocketDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper;

    @Override
    public void publishNewMessage(String topic, WebSocketDto webSocketDto) throws JsonProcessingException {

        if (!webSocketDto.checkValidation()) {
            throw new IllegalArgumentException("Invalid WebSocketDto : " + webSocketDto);
        }

        redisTemplate.convertAndSend(topic, mapper.writeValueAsString(webSocketDto));
    }
}
