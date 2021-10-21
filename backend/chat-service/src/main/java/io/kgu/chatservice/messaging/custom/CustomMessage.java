package io.kgu.chatservice.messaging.custom;

import io.kgu.chatservice.domain.entity.MessageContentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CustomMessage implements Serializable {

    private MessageContentType type;
    private byte[] payload;

    public CustomMessage(TextMessage message) {
        this.type = MessageContentType.TEXT;
        this.payload = message.asBytes();
    }

    public CustomMessage(BinaryMessage message) {
        this.type = MessageContentType.IMAGE;
        this.payload = message.getPayload().array();
    }

    public Object toMessage() {
        if (type == MessageContentType.TEXT) {
            return new TextMessage(payload);
        } else if (type == MessageContentType.IMAGE) {
            return new BinaryMessage(payload);
        } else {
            return null;
        }
    }

}
