package io.kgu.chatservice.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    public CustomAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal, Object credentials) {

        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true); // 권한

    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}
