package io.kgu.randhandserver.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.randhandserver.domain.dto.UserDto;
import io.kgu.randhandserver.domain.entity.User;
import io.kgu.randhandserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Transactional
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HttpSession httpSession;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        User user = (User) httpSession.getAttribute("user");
        UserDto userDto = userService.dto(user);


        redisTemplate.opsForHash().put("user:"+httpSession.getId(), "entity", userDto);
        redisTemplate.expire("user:"+httpSession.getId(), 30, TimeUnit.MINUTES);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), httpSession.getId());
    }
}
