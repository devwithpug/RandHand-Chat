package io.kgu.chatservice.socket.custom;

import io.kgu.chatservice.domain.entity.MessageContentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class WebSocketDto implements Serializable {

    private Integer serviceId;
    private String destinationId;
    private MessageContentType type;
    private CustomWebSocketSession session;
    private CustomMessage message;

    public boolean checkValidation() {
        return type != null && session != null && message != null;
    }

    public static WebSocketDto of(
            Integer serviceId, String destinationId, StandardWebSocketSession session, TextMessage message
    ) {
        WebSocketDto socketDto = new WebSocketDto();

        socketDto.setServiceId(serviceId);
        socketDto.setDestinationId(destinationId);
        socketDto.setType(MessageContentType.TEXT);
        socketDto.setSession(new CustomWebSocketSession(session));
        socketDto.setMessage(new CustomMessage(message));

        return socketDto;
    }

    public static WebSocketDto of(
            Integer serviceId, String destinationId, StandardWebSocketSession session, BinaryMessage message
    ) {
        WebSocketDto socketDto = new WebSocketDto();

        socketDto.setServiceId(serviceId);
        socketDto.setDestinationId(destinationId);
        socketDto.setType(MessageContentType.IMAGE);
        socketDto.setSession(new CustomWebSocketSession(session));
        socketDto.setMessage(new CustomMessage(message));

        return socketDto;
    }
}
