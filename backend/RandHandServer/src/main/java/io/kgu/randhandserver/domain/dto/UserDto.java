package io.kgu.randhandserver.domain.dto;

import io.kgu.randhandserver.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class UserDto implements Serializable {

    private Long id;
    private String auth;
    private String email;
    private String name;
    private String statusMessage;
    private String picture;
    private UserInfoDto userInfo;
    private List<Long> userFriends;
    private List<Long> userBlocked;
    private String role;

    public static UserDto of(User user) {
        return UserDto.builder()
                .id(user.getId())
                .auth(user.getAuth())
                .email(user.getEmail())
                .name(user.getName())
                .statusMessage(user.getUserInfo().getStatusMessage())
                .picture(user.getUserInfo().getPicture())
                .userInfo(UserInfoDto.of(user))
                .userFriends(user.getUserFriends())
                .userBlocked(user.getUserBlocked())
                .role(user.getRole().name())
                .build();
    }

}
