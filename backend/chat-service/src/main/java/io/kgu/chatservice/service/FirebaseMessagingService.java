package io.kgu.chatservice.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import io.kgu.chatservice.domain.dto.MessageDto;

public interface FirebaseMessagingService {

    String sendNotification(MessageDto messageDto, String token) throws FirebaseMessagingException;

}
