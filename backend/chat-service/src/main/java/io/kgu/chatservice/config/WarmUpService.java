package io.kgu.chatservice.config;

import com.netflix.appinfo.InstanceInfo;
import io.kgu.chatservice.domain.request.RequestLogin;
import io.kgu.chatservice.domain.request.RequestUser;
import io.kgu.chatservice.domain.response.ResponseUser;
import io.kgu.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
@RequiredArgsConstructor
public class WarmUpService {

    private final UserService userService;

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void onApplicationEvent() {

        log.info("Initializing Chat-service warm up");

        RequestUser req = RequestUser.builder().email("t@t").auth("t").picture("t").name("t").build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");
        headers.add("Accept-Encoding", "gzip, deflate, br");

        HttpEntity<RequestUser> requestUserHttpEntity = new HttpEntity<>(req, headers);

        RestTemplate rt = new RestTemplate();

        int statusCode = -1;

        while (statusCode != 200) {
            try {
                Thread.sleep(10000);
                statusCode = rt.getForEntity("http://localhost:8000/chat-service/actuator/health", String.class).getStatusCodeValue();
            } catch (InterruptedException | HttpServerErrorException e) {
                log.warn("No servers available now. Retry in next 10 seconds.");
            }
        }

        long startTime = System.currentTimeMillis();

        log.info("Chat-service was registered and Connected with Gateway-service");
        log.info("Warm up with RestTemplate call");

        ResponseEntity<ResponseUser> response = rt.postForEntity(
                "http://localhost:8000/chat-service/users",
                requestUserHttpEntity,
                ResponseUser.class
        );
        String userId = response.getBody().getUserId();

        RequestLogin login = new RequestLogin();
        login.setUserId(userId);
        login.setEmail("t@t");

        HttpEntity<RequestLogin> requestLoginHttpEntity = new HttpEntity<>(login, headers);

        rt.postForEntity(
                "http://localhost:8000/chat-service/login",
                requestLoginHttpEntity,
                ResponseUser.class
        );

        userService.deleteUser(userId);

        log.info("Completed Chat-service warm up in {} ms", System.currentTimeMillis() - startTime);

    }
}
