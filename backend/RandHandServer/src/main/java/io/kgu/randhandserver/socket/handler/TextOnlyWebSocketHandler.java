package io.kgu.randhandserver.socket.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TextOnlyWebSocketHandler extends TextWebSocketHandler {

    private static Set<WebSocketSession> sessions = new ConcurrentHashMap().newKeySet();

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
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.remove(session);
        log.info("client{} disconnect", session.getRemoteAddress());
    }
}
