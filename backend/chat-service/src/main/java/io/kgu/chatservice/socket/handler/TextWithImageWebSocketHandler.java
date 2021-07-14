package io.kgu.chatservice.socket.handler;

import io.kgu.chatservice.domain.dto.MessageDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.domain.entity.MessageEntity;
import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.nio.file.AccessDeniedException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Transactional
public class TextWithImageWebSocketHandler extends AbstractWebSocketHandler {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ModelMapper modelMapper;

    private static Set<WebSocketSession> sessions = new ConcurrentHashMap().newKeySet();

    @Autowired
    public TextWithImageWebSocketHandler(MessageRepository messageRepository, ChatRepository chatRepository, ModelMapper modelMapper) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        if (session.getUri().getPath().equals("/websocket/session/")) {
            throw new AccessDeniedException("sessionId 가 없습니다.");
        }
        
        super.afterConnectionEstablished(session);
        sessions.add(session);
        log.info("client{} connect", session.getRemoteAddress());

        for (WebSocketSession webSocketSession : sessions) {
            if (session == webSocketSession || !session.getUri().equals(webSocketSession.getUri())) continue;
            webSocketSession.sendMessage(new TextMessage("상대방이 입장했습니다.", true));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("client{} message : {}", session.getRemoteAddress(), message.getPayload());
        for (WebSocketSession webSocketSession : sessions) {
            if (session == webSocketSession || !session.getUri().equals(webSocketSession.getUri())) continue;
            storeMessage(session, message);
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

        log.info(message.toString());
        for (WebSocketSession webSocketSession : sessions) {
            if (session == webSocketSession || !session.getUri().equals(webSocketSession.getUri())) continue;
            storeMessage(session, message);
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.remove(session);
        log.info("client{} disconnect", session.getRemoteAddress());
    }

    private void storeMessage(WebSocketSession sender, AbstractWebSocketMessage<?> message) throws AccessDeniedException {

        String sessionId = sender.getUri().getPath().replace("/websocket/session/", "");
        ChatEntity chatRoom = chatRepository.findBySessionId(sessionId);

        if (chatRoom == null) {
            throw new IllegalArgumentException("ChatRoom 존재 하지 않음");
        }

        if (!sender.getHandshakeHeaders().containsKey("from") || !sender.getHandshakeHeaders().containsKey("to") || message == null) {
            throw new AccessDeniedException("잘못된 메세지 전송 요청");
        }

        String from = sender.getHandshakeHeaders().get("from").get(0);
        String to = sender.getHandshakeHeaders().get("to").get(0);

        if (!chatRoom.getUserIds().contains(from) || !chatRoom.getUserIds().contains(to)) {
            throw new AccessDeniedException("ChatRoom 구성원이 아닙니다.");
        }

        MessageDto messageDto = MessageDto.builder()
                .sessionId(sessionId)
                .fromUser(from)
                .toUser(to)
                .build();

        if (message instanceof TextMessage) {
            messageDto.setContent(((TextMessage)message).getPayload());
        } else if (message instanceof BinaryMessage) {
            messageDto.setContent("[Binary data]");
        } else {
            throw new ClassFormatError("잘못된 메세지 포맷");
        }

        messageRepository.save(modelMapper.map(messageDto, MessageEntity.class));

    }

}
