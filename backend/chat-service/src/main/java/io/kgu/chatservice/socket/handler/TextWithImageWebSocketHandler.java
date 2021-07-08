package io.kgu.chatservice.socket.handler;

import io.kgu.chatservice.domain.entity.Message;
import io.kgu.chatservice.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
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

    private static Set<WebSocketSession> sessions = new ConcurrentHashMap().newKeySet();

    @Autowired
    public TextWithImageWebSocketHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
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

            webSocketSession.sendMessage(message);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

        log.info(message.toString());
        for (WebSocketSession webSocketSession : sessions) {
            if (session == webSocketSession || !session.getUri().equals(webSocketSession.getUri())) continue;
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.remove(session);
        log.info("client{} disconnect", session.getRemoteAddress());
    }


    // TODO - 메시지 DB 로그 남기기
    private void storeMessage(WebSocketSession sender, AbstractWebSocketMessage<?> message) throws AccessDeniedException {

        // TODO - from & to 검증
        if (!sender.getHandshakeHeaders().containsKey("from") || !sender.getHandshakeHeaders().containsKey("to") || message == null) {
            throw new AccessDeniedException("잘못된 메세지 전송 요청");
        }

        String from = sender.getHandshakeHeaders().get("from").get(0);
        String to = sender.getHandshakeHeaders().get("to").get(0);

        Message msg = null;

        if (message instanceof TextMessage) {

            msg = Message.builder()
                    .fromUser(from)
                    .toUser(to)
                    .content(((TextMessage) message).getPayload())
                    .build();

        } else if (message instanceof BinaryMessage) {
            msg = Message.builder()
                    .fromUser(from)
                    .toUser(to)
                    .content("[Binary data]")
                    .build();
        } else {
            throw new ClassFormatError("잘못된 메세지 포맷");
        }

        messageRepository.save(msg);

    }

}
