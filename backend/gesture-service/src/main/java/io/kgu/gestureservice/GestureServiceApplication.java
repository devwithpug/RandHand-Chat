package io.kgu.gestureservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestureServiceApplication.class, args);
    }

    // TODO - 제스쳐 데이터 받아서 채팅 큐로 관리
    // TODO - 제스쳐 매칭 성공 시 chat-service 에 세션 생성 위임
    // TODO - 매칭 실패 시 DB에 저장 & 다른 데이터들과 비교

}
