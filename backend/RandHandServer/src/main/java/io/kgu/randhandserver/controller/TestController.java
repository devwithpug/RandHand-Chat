package io.kgu.randhandserver.controller;

import io.kgu.randhandserver.domain.dto.UserDto;
import io.kgu.randhandserver.domain.entity.User;
import io.kgu.randhandserver.security.config.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final HttpSession httpSession;

    @GetMapping("/websocket/test/text")
    public String webSocketText() {
        return "websocket_text";
    }

    @GetMapping("/websocket/test/image")
    public String webSocketImage() {
        return "websocket_image";
    }

    @GetMapping("/test/user")
    @ResponseBody
    public User user(@LoginUser User user) {
        return user;
    }

    @GetMapping("/test/userdto")
    @ResponseBody
    public UserDto userDto(@LoginUser User user) {
        return UserDto.of(user);
    }

    @GetMapping("/test/token")
    public void token(HttpServletRequest request, HttpSession httpSession) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println();
    }

}
