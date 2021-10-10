package io.kgu.chatservice.socket.handler;

import io.kgu.chatservice.domain.dto.MessageDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TextWithImageWebSocketHandler extends AbstractWebSocketHandler implements MessageListener {

    private final MessageService messageService;
    private final ChatRepository chatRepository;

    private final RedisMessageListenerContainer container;

    // 웹소켓 세션 관리
    private static final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // ELB 를 통해 현재 EC2 인스턴스에 접속한 유저 관리
    private static final Map<String, Set<String>> users = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        if (session.getUri().getPath().equals("/websocket/session/")) {
            throw new AccessDeniedException("sessionId 가 없습니다.");
        } else if (!session.getHandshakeHeaders().containsKey("userId")) {
            throw new AccessDeniedException("userId 가 없습니다.");
        }
        String sessionId = session.getUri().getPath().replace("/websocket/session/", "");
        String userId = session.getHandshakeHeaders().get("userId").get(0);
        super.afterConnectionEstablished(session);

        // 유저가 동일한 웹소켓 서버에 접속한 경우
        if (users.containsKey(sessionId)) {
            Set<String> set = users.get(sessionId);
            set.add(userId);
            users.put(sessionId, set);
        } else {
            users.put(sessionId, Set.of(userId));

            // TODO - Subscribe new Redis topic
        }

        sessions.add(session);
        log.info("client{} connect", session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("client{} message : {}", session.getRemoteAddress(), message.getPayload());

        if (!session.getHandshakeHeaders().containsKey("debug")) {
            verifyAndSaveMessage(session, message);
        }

        // 동일한 웹소켓 서버에 접속하지 않은 경우
        // Redis 통하여 메시지 publish
        // TODO -

        for (WebSocketSession webSocketSession : sessions) {
            if (session == webSocketSession || !session.getUri().equals(webSocketSession.getUri())) continue;
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

        if (!session.getHandshakeHeaders().containsKey("debug")) {
            MessageDto messageDto = verifyAndSaveMessage(session, message);
            // receiver 에게 S3에 업로드된 Image url 전송
            message = new BinaryMessage(messageDto.getContent().getBytes(StandardCharsets.UTF_8));
        }

        // TODO
        // 동일한 웹소켓 서버에 접속하지 않은 경우
        // Redis 통하여 메시지 publish

        // 업로드가 완료된 이미지의 Image url 이므로 sender 에게도 전송
        for (WebSocketSession webSocketSession : sessions) {
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        String sessionId = session.getUri().getPath().replace("/websocket/session/", "");
        String userId = session.getHandshakeHeaders().get("userId").get(0);

        if (userId == null) {
            throw new IllegalStateException("웹소켓 연결을 닫는 중 예외 발생 : userId 값이 없습니다.");
        }

        Set<String> set = users.get(sessionId);
        // 두 클라이언트 모두 웹소켓에 연결 해제한 경우
        if (set.size() == 1) {
            users.remove(sessionId);

            // TODO - Unsubscribe Redis topic


        } else {
            set.remove(userId);
            users.put(sessionId, set);
        }

        sessions.remove(session);
        log.info("client{} disconnect", session.getRemoteAddress());
    }

    private MessageDto verifyAndSaveMessage(WebSocketSession sender, AbstractWebSocketMessage<?> message) {

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

        MessageDto result = null;
        try {
            result = messageService.create(message, chatRoom, from);

            chatRoom.setSyncTime(result.getCreatedAt());
            chatRepository.save(chatRoom);
        } catch (IllegalArgumentException | IOException | ClassFormatError ex) {
            log.error(ex.getMessage());
        }

        return result;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        // TODO - sessions 를 순환하면서 유저에게 웹소켓 메시지 전송

    }

    private void subscribeNewTopic(String topic) {
        container.addMessageListener(this, ChannelTopic.of(topic));
    }

    private void unsubscribeTopic(String topic) {
        container.removeMessageListener(this, ChannelTopic.of(topic));
    }
}
