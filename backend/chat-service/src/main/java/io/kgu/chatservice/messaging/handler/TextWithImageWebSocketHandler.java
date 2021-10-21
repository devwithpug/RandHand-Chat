package io.kgu.chatservice.messaging.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.chatservice.domain.dto.chat.MessageDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.domain.entity.MessageContentType;
import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.service.MessageService;
import io.kgu.chatservice.service.RedisService;
import io.kgu.chatservice.messaging.custom.WebSocketDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.*;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Transactional
public class TextWithImageWebSocketHandler extends AbstractWebSocketHandler implements MessageListener {

    @Autowired
    private MessageService messageService;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RedisMessageListenerContainer container;
    @Autowired
    private ServletWebServerApplicationContext context;
    @Autowired
    private ObjectMapper mapper;


    // ELB 를 통해 현재 EC2 인스턴스에 접속한 유저의 세션 관리
    private static final Map<String, WebSocketSession> users = new ConcurrentHashMap<>();


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

        // Subscribe Redis Topic (chat room sessionId)
        container.addMessageListener(this, ChannelTopic.of(sessionId));

        // 웹소켓 서버에 접속한 경우 WsSession 값 매핑하여 저장
        users.put(userId, session);
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
        String toUser = session.getHandshakeHeaders().get("toUser").get(0);

        // 현재 인스턴스에 유저가 있는 경우
        if (users.containsKey(toUser)) {
            WebSocketSession toSession = users.get(toUser);
            toSession.sendMessage(message);
        } else {
            // publish Message with Redis
            String sessionId = session.getUri().getPath().replace("/websocket/session/", "");
            int serviceId = context.getWebServer().getPort();
            redisService.publishNewMessage(
                    sessionId,  WebSocketDto.of(serviceId, toUser, (StandardWebSocketSession) session, message)
            );
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

        if (!session.getHandshakeHeaders().containsKey("debug")) {
            MessageDto messageDto = verifyAndSaveMessage(session, message);
            // receiver 에게 S3에 업로드된 Image url 전송
            message = new BinaryMessage(messageDto.getContent().getBytes(StandardCharsets.UTF_8));
        }

        // 동일한 웹소켓 서버에 접속하지 않은 경우
        // Redis 통하여 메시지 publish
        String toUser = session.getHandshakeHeaders().get("toUser").get(0);

        if (users.containsKey(toUser)) {
            WebSocketSession toSession = users.get(toUser);
            toSession.sendMessage(message);
        } else {
            String sessionId = session.getUri().getPath().replace("/websocket/session/", "");
            int serviceId = context.getWebServer().getPort();
            redisService.publishNewMessage(
                    sessionId,  WebSocketDto.of(serviceId, toUser, (StandardWebSocketSession) session, message)
            );
        }
        // 업로드가 완료된 이미지의 Image url 이므로 sender 에게도 전송
        session.sendMessage(message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        String userId = session.getHandshakeHeaders().get("userId").get(0);

        if (userId == null) {
            throw new IllegalStateException("웹소켓 연결을 닫는 중 예외 발생 : userId 값이 없습니다.");
        }
        String sessionId = session.getUri().getPath().replace("/websocket/session/", "");

        // Subscribe Redis Topic (chat room sessionId)
        container.removeMessageListener(this, ChannelTopic.of(sessionId));

        users.remove(userId);
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


    // Redis subscribe message
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            WebSocketDto dto = mapper.readValue(message.toString(), WebSocketDto.class);

            String toUser = dto.getDestinationId();

            if (context.getWebServer().getPort() != dto.getServiceId()) {
                if (users.containsKey(toUser)) {
                    WebSocketSession session = users.get(toUser);
                    Object msg = dto.getMessage().toMessage();
                    session.sendMessage(
                            dto.getType() == MessageContentType.TEXT ? (TextMessage) msg : (BinaryMessage) msg
                    );
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("JSON mapper 파싱 에러 : " + e);
        }
    }

}
