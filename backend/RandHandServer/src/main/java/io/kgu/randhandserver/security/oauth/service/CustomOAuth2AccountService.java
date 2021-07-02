package io.kgu.randhandserver.security.oauth.service;

import io.kgu.randhandserver.domain.entity.User;
import io.kgu.randhandserver.domain.entity.UserInfo;
import io.kgu.randhandserver.repository.UserInfoRepository;
import io.kgu.randhandserver.repository.UserRepository;
import io.kgu.randhandserver.security.oauth.dto.OAuthAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2AccountService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(registrationId, attributes);

        httpSession.setAttribute("user", user);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(String registrationId, OAuthAttributes attributes) {

        User user = userRepository.getOneByAuthAndEmail(registrationId, attributes.getEmail())
                .map(entity -> {
                    entity.setName(attributes.getName());
                    entity.setPicture(attributes.getPicture());
                    return entity;
                }).orElse(attributes.toEntity());

        if (user.getUserInfo() == null) {
            UserInfo info = userInfoRepository.save(UserInfo.create(user));
            user.setUserInfo(info);
        }

        return userRepository.save(user);
    }
}
