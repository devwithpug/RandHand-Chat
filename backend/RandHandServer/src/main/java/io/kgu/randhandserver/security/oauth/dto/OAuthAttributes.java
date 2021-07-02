package io.kgu.randhandserver.security.oauth.dto;

import io.kgu.randhandserver.domain.entity.Role;
import io.kgu.randhandserver.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String auth;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes) {

        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        } else if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        } else {
            return ofGoogle(userNameAttributeName, attributes);
        }

    }

    public static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {

        attributes = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .auth("naver")
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("profile_image"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();

    }

    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {

        return OAuthAttributes.builder()
                .auth("google")
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();

    }

    public static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {

        LinkedHashMap properties = (LinkedHashMap) attributes.get("properties");
        LinkedHashMap account = (LinkedHashMap) attributes.get("kakao_account");

        return OAuthAttributes.builder()
                .auth("kakao")
                .name((String) properties.get("nickname"))
                .email((String) account.get("email"))
                .picture((String) properties.get("thumbnail_image"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();

    }

    public User toEntity() {

        return User.builder()
                .auth(auth)
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.USER)
                .build();

    }

}
