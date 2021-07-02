package io.kgu.randhandserver.socket;


import io.kgu.randhandserver.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@ServerEndpoint("/websocket/tomcat")
public class Socket {

    private Session session;
    public static Set<Socket> listeners = new CopyOnWriteArraySet<>(); // thread safe Set
    private static int onlineCount = 0;

    @OnOpen // 클라이언트가 소켓에 연결될때 마다 호출
    public void onOpen(Session session) {
        onlineCount++;
        this.session = session;
        listeners.add(this);
        log.info("onOpen, userCount : " + onlineCount);
    }

    @OnClose
    public void onClose(Session session) {
        onlineCount--;
        listeners.remove(this);
        log.info("onClose, userCount : " + onlineCount);
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("message : " + message);
        broadcast(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.warn(session.getId() + " : " + throwable.getMessage());
        listeners.remove(this);
        onlineCount--;
    }

    public static void broadcast(String message) {
        for (Socket listener : listeners) {
            listener.sendMessage(message);
        }
    }

    private void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.warn(this.session.getId() + " : " + e.getMessage());
        }
    }

}
