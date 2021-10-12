package io.kgu.chatservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final ServletWebServerApplicationContext context;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {

        InetAddress localHost = null;
        String host;
        int port = context.getWebServer().getPort();

        try {
            localHost = InetAddress.getLocalHost();
            host = localHost.toString();
        } catch (UnknownHostException e) {
            host = "unknown";
        }

        return ResponseEntity.ok(String.format("Host: %s\nPort: %s", host, port));
    }
}
