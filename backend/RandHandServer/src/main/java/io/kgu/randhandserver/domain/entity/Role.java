package io.kgu.randhandserver.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    ADMIN("ROLE_ADMIN", "관리자"),
    USER("ROLE_GUEST", "사용자");

    private final String key;
    private final String title;

}
