package io.kgu.chatservice.socket.handler;

import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.nio.file.AccessDeniedException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TextWithImageWebSocketHandler extends AbstractWebSocketHandler {

    private final MessageService messageService;
    private final ChatRepository chatRepository;

    private static Set<WebSocketSession> sessions = new ConcurrentHashMap().newKeySet();

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

        if (!session.getHandshakeHeaders().containsKey("debug")) {
            verifyAndSaveMessage(session, message);
        }

        for (WebSocketSession webSocketSession : sessions) {
            if (session == webSocketSession || !session.getUri().equals(webSocketSession.getUri())) continue;
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

        if (!session.getHandshakeHeaders().containsKey("debug")) {
            verifyAndSaveMessage(session, message);
        }

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

    private void verifyAndSaveMessage(WebSocketSession sender, AbstractWebSocketMessage<?> message) throws AccessDeniedException {

        String sessionId = sender.getUri().getPath().replace("/websocket/session/", "");
        String from = "";
        ChatEntity chatRoom = null;

        if (!sender.getHandshakeHeaders().containsKey("skip-validate-session")) {
            chatRoom = chatRepository.findChatEntityBySessionId(sessionId);

            if (chatRoom == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ChatRoom 존재 하지 않음");
            }

            if (!sender.getHandshakeHeaders().containsKey("userId") || message == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 메세지 전송 요청");
            }

            from = sender.getHandshakeHeaders().get("userId").get(0);

            if (!chatRoom.getUserIds().contains(from)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ChatRoom 구성원이 아닙니다.");
            }
        }

        messageService.create(message, chatRoom, from);

    }

}
