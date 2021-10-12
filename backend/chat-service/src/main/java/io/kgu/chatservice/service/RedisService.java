package io.kgu.chatservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.kgu.chatservice.socket.custom.WebSocketDto;

public interface RedisService {

    void publishNewMessage(String topic, WebSocketDto webSocketDto) throws JsonProcessingException;

}
