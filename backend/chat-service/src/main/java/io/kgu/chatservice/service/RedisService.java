package io.kgu.chatservice.service;


public interface RedisService {

    void publishNewMessage(String topic, String base64String);

}
