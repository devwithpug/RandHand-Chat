package io.kgu.chatservice.scheduler;

import io.kgu.chatservice.domain.dto.user.UserDto;
import io.kgu.chatservice.domain.dto.user.RequestLoginDto;
import io.kgu.chatservice.domain.dto.user.RequestUserDto;
import io.kgu.chatservice.domain.dto.user.ResponseUserDto;
import io.kgu.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Slf4j
@Component
@Setter @Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "randhand")
public class WarmUpScheduler {

    private final UserService userService;

    @NotEmpty private String hostIp;
    @NotNull private Boolean warmUp;

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void onApplicationEvent() {

        if (warmUp) {

            log.info("Initializing Chat-service warm up");

            RestTemplate rt = new RestTemplate();

            int statusCode = -1;

            while (statusCode != HttpStatus.SC_OK) {
                try {
                    Thread.sleep(10000);
                    statusCode = rt.getForEntity("http://" + hostIp + ":8000/chat-service/actuator/health", String.class).getStatusCodeValue();
                } catch (InterruptedException | HttpServerErrorException | ResourceAccessException e) {
                    log.warn("No servers available now. Retry in next 10 seconds.");
                }
            }

            log.info("Chat-service was registered and Connected with Gateway-service");
            log.info("Warm up with RestTemplate call");

            String email = (RandomStringUtils.randomAlphabetic(3)
                    + "@" + RandomStringUtils.randomAlphabetic(3)
                    + "." + RandomStringUtils.randomAlphabetic(3)
            ).toLowerCase();

            try {

                long startTime = System.currentTimeMillis();


                RequestUserDto requestCreateUser = RequestUserDto.builder().email(email).auth("t").name("t").build();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/json");
                headers.add("Accept", "application/json");
                headers.add("Accept-Encoding", "gzip, deflate, br");

                ResponseEntity<ResponseUserDto> resp = warmUpCreateUser(rt, headers, requestCreateUser);

                String userId = resp.getBody().getUserId();

                RequestLoginDto requestLoginDtoUser = new RequestLoginDto();
                requestLoginDtoUser.setUserId(userId);
                requestLoginDtoUser.setEmail(email);

                resp = warmUpLoginUser(rt, headers, requestLoginDtoUser);

                String token = resp.getHeaders().get("token").get(0);

                headers.add("userId", userId);
                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

                warmUpGetUserInfoWithAuthAndEmail(email, rt, headers);
                warmUpGetUserInfoWithUserId(rt, headers, userId);
                warmUpDeleteUser(rt, headers, userId);

                log.info("Completed Chat-service warm up in {} ms", System.currentTimeMillis() - startTime);


            } catch (HttpServerErrorException | HttpClientErrorException e) {
                log.warn("Chat-service warm up was not completed perfectly");

            }

            try {
                UserDto warmUpUser = userService.getUserByAuthAndEmail("t", email);
                userService.deleteUser(warmUpUser.getUserId());
                log.info("Warm up Entity was deleted successfully");
            } catch (UsernameNotFoundException ex) {
            }

        } else {
            log.info("Chat-service warm up skipped");
        }
    }

    private ResponseEntity<ResponseUserDto> warmUpCreateUser(RestTemplate rt, HttpHeaders headers, RequestUserDto req) throws HttpServerErrorException {
        HttpEntity<RequestUserDto> requestUserHttpEntity = new HttpEntity<>(req, headers);

        ResponseEntity<ResponseUserDto> response = rt.postForEntity(
                "http://" + hostIp + ":8000/chat-service/users",
                requestUserHttpEntity,
                ResponseUserDto.class
        );

        if (response.getStatusCodeValue() != HttpStatus.SC_CREATED) {
            throw new HttpServerErrorException(response.getStatusCode());
        }

        return response;
    }

    private ResponseEntity<ResponseUserDto> warmUpLoginUser(RestTemplate rt, HttpHeaders headers, RequestLoginDto req) throws HttpServerErrorException {

        HttpEntity<RequestLoginDto> requestLoginHttpEntity = new HttpEntity<>(req, headers);

        ResponseEntity<ResponseUserDto> response = rt.postForEntity(
                "http://" + hostIp + ":8000/chat-service/login",
                requestLoginHttpEntity,
                ResponseUserDto.class
        );

        if (response.getStatusCodeValue() != HttpStatus.SC_OK) {
            throw new HttpServerErrorException(response.getStatusCode());
        }

        return response;
    }

    private void warmUpGetUserInfoWithAuthAndEmail(String email, RestTemplate rt, HttpHeaders headers) {

        headers.add("auth", "t");
        headers.add("email", email);

        rt.exchange(
                "http://" + hostIp + ":8000/chat-service/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseUserDto.class
        );

    }

    private void warmUpGetUserInfoWithUserId(RestTemplate rt, HttpHeaders headers, String userId) {

        rt.exchange(
                "http://" + hostIp + ":8000/chat-service/users/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

    }

    private void warmUpDeleteUser(RestTemplate rt, HttpHeaders headers, String userId) {

        rt.exchange(
                "http://" + hostIp + ":8000/chat-service/users/" + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );

    }
}

