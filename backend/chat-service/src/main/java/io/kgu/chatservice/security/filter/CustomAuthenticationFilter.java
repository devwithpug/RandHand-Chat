package io.kgu.chatservice.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.kgu.chatservice.domain.dto.UserDto;
import io.kgu.chatservice.domain.request.RequestLogin;
import io.kgu.chatservice.security.token.CustomAuthenticationToken;
import io.kgu.chatservice.service.UserService;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final UserService userService;
    private final Environment env;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, Environment env) {
        super("/login", authenticationManager);
        this.userService = userService;
        this.env = env;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        try {

            RequestLogin credentials = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);
            return getAuthenticationManager().authenticate(
                new CustomAuthenticationToken(new ArrayList<>(), credentials.getEmail(), credentials.getUserId())
            );

        } catch (IOException e) {
            throw new BadCredentialsException(e.getMessage());
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        String email = (String) authResult.getPrincipal();
        UserDto userDetails = userService.getUserByEmail(email);

        String token = Jwts.builder()
                .setSubject(userDetails.getUserId())
                .setExpiration(new Date(System.currentTimeMillis() +
                        Long.parseLong(env.getProperty("token.expiration_time"))))
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
                .compact();

        response.addHeader("token", token);
        response.addHeader("userId", userDetails.getUserId());

    }
}
