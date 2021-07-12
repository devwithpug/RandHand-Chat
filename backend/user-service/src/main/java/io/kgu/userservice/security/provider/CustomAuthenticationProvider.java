package io.kgu.userservice.security.provider;

import io.kgu.userservice.domain.dto.UserDto;
import io.kgu.userservice.security.token.CustomAuthenticationToken;
import io.kgu.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String email = (String) authentication.getPrincipal();
        String userId = (String) authentication.getCredentials();

        UserDetails userDetails = userService.loadUserByUsername(userId);
        UserDto userDto = userService.getUserByUserId(userId);

        if (!userDto.getEmail().equals(email)) {
            throw new BadCredentialsException("BadCredentialsException");
        }

        return new CustomAuthenticationToken(userDetails.getAuthorities(), email, userId);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
