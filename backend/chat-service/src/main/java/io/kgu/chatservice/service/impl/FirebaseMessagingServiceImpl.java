package io.kgu.chatservice.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import io.kgu.chatservice.domain.dto.MessageDto;
import io.kgu.chatservice.domain.dto.UserDto;
import io.kgu.chatservice.service.FirebaseMessagingService;
import io.kgu.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static io.kgu.chatservice.domain.entity.MessageContentType.TEXT;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseMessagingServiceImpl implements FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;
    private final UserService userService;

    @Override
    public String sendNotification(MessageDto messageDto, String token) throws FirebaseMessagingException {

        UserDto userDto = userService.getUserByUserId(messageDto.getFromUser());

        Notification notification = Notification.builder()
                .setTitle(userDto.getName())
                .setBody((messageDto.getType() == TEXT) ? messageDto.getContent() : "[IMAGE]")
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        return firebaseMessaging.send(message);
    }
}
