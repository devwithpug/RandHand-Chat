package io.kgu.chatservice.messaging.custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomWebSocketSession implements Serializable {

    private String id;

    @Nullable
    private URI uri;

    private HttpHeaders handshakeHeaders;

    @Nullable
    private String acceptedProtocol;

    @Nullable
    private List<CustomWebSocketExtension> extensions;

    @Nullable
    private Principal user;

    @Nullable
    private InetSocketAddress localAddress;

    @Nullable
    private InetSocketAddress remoteAddress;

    public CustomWebSocketSession(StandardWebSocketSession session) {
        this.id = session.getId();
        this.uri = session.getUri();
        this.handshakeHeaders = session.getHandshakeHeaders();
        this.acceptedProtocol = session.getAcceptedProtocol();
        List<WebSocketExtension> extensions = session.getExtensions();
        List<CustomWebSocketExtension> customExtensions = new ArrayList<>();
        for (WebSocketExtension extension : extensions) {
            customExtensions.add(new CustomWebSocketExtension(extension.getName(), extension.getParameters()));
        }
        this.extensions = customExtensions;
        this.user = session.getPrincipal();
        this.localAddress = session.getLocalAddress();
        this.remoteAddress = session.getRemoteAddress();
    }

    public StandardWebSocketSession extractSession() {
        return new StandardWebSocketSession(
                this.handshakeHeaders,
                null,
                this.localAddress,
                this.remoteAddress,
                this.user
        );
    }

}
