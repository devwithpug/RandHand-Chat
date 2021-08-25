package io.kgu.chatservice.socket.config;

import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.service.MessageService;
import io.kgu.chatservice.socket.handler.TextWithImageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageService messageService;
    private final ChatRepository chatRepository;

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new TextWithImageWebSocketHandler(messageService, chatRepository);
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxBinaryMessageBufferSize(3072000);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/websocket/session/**")
                .setAllowedOrigins("*");
    }
}
