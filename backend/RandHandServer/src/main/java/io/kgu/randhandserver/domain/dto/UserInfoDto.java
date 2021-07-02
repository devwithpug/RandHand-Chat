package io.kgu.randhandserver.domain.dto;

import io.kgu.randhandserver.domain.entity.User;
import io.kgu.randhandserver.domain.entity.UserInfo;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class UserInfoDto implements Serializable {

    private String statusMessage;
    private String picture;

    public static UserInfoDto of(User user) {

        return UserInfoDto.builder()
                .statusMessage(user.getUserInfo().getStatusMessage())
                .picture(user.getUserInfo().getPicture())
                .build();

    }

}
