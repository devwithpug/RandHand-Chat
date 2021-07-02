package io.kgu.randhandserver.security.config;

import io.kgu.randhandserver.domain.entity.Role;
import io.kgu.randhandserver.security.handler.OAuth2AuthenticationSuccessHandler;
import io.kgu.randhandserver.security.oauth.service.CustomOAuth2AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2AccountService customOAuth2AccountService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/login", "/login/**", "/websocket/**", "/test/**")
                .permitAll()
                .antMatchers("/**")
                .hasRole(Role.USER.name())
                .anyRequest()
                .authenticated()
        ;
        http
                .oauth2Login()
                .userInfoEndpoint()
                .userService(customOAuth2AccountService)
                .and()
                .successHandler(authenticationSuccessHandler())
        ;

    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler();
    }
}
