package io.kgu.chatservice.socket.handler;

import io.kgu.chatservice.domain.dto.MessageDto;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            MessageDto messageDto = verifyAndSaveMessage(session, message);
            // receiver 에게 S3에 업로드된 Image url 전송
            message = new BinaryMessage(messageDto.getContent().getBytes(StandardCharsets.UTF_8));
        }

        log.info(message.toString());
        // 업로드가 완료된 이미지의 Image url 이므로 sender 에게도 전송
        for (WebSocketSession webSocketSession : sessions) {
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
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

}
