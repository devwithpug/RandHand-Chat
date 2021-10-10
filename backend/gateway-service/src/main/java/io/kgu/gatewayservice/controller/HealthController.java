package io.kgu.gatewayservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final Environment env;

    @GetMapping("/health")
    public Flux<String> healthCheck() {

        InetAddress localHost = null;
        String host;
        String port = env.getProperty("server.port");

        try {
            localHost = InetAddress.getLocalHost();
            host = localHost.toString();
        } catch (UnknownHostException e) {
            host = "unknown";
        }

        return Flux.just(String.format("Host: %s\nPort: %s", host, port));
    }

}
