package io.kgu.chatservice.service.impl;

import io.kgu.chatservice.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void publishNewMessage(String topic, String base64String) {

        // 해당 로직으로 구현 불가능
        if (!Base64.isBase64(base64String)) {
            throw new IllegalArgumentException("Base64 인코딩 된 데이터가 아닙니다.");
        }

        // TODO - ObjectMapper 사용하여 구현

        redisTemplate.convertAndSend(topic, base64String);
    }
}
