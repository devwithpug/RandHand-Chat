package io.kgu.randhandserver.socket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.randhandserver.domain.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import javax.servlet.http.HttpSession;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TextWithImageWebSocketHandler extends AbstractWebSocketHandler {

    private static Set<WebSocketSession> sessions = new ConcurrentHashMap().newKeySet();
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.add(session);
        log.info("client{} connect", session.getRemoteAddress());

        try {
            List<String> sessionKey = session.getHandshakeHeaders().get("session-key");

            // TODO - 세션에서 얻은 userDto 이용하여 검증 & 메시지 전송자 명시 필요
            UserDto userDto = objectMapper.convertValue(redisTemplate.opsForHash().get("user:" + sessionKey.get(0), "entity"), UserDto.class);
        } catch (Exception e) {
            log.warn("인증된 세션이 존재하지 않습니다.");
        }

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

}
