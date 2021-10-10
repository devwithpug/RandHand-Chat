package io.kgu.chatservice.service.impl;

import io.kgu.chatservice.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void publishNewMessage(String topic, String base64String) {

        // TODO - publishMessage

    }
}
