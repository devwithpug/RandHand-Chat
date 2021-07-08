package io.kgu.userservice.domain.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class RequestUser {

    private String userId;

    @NotNull(message = "OAuth2 인증 서버가 존재하지 않습니다!")
    private String auth;

    @NotNull(message = "이메일이 존재하지 않습니다!")
    @Size(min = 2, message = "이메일은 최소 2자 이상이어야 합니다.")
    @Email
    private String email;

    @NotNull(message = "이름이 존재하지 않습니다!")
    @NotBlank(message = "이름이 비어있습니다.")
    private String name;

    @NotNull(message = "프로필 사진이 존재하지 않습니다!")
    private String picture;

    @Size(max = 50, message = "상태 메시지는 최대 50자 입니다.")
    private String statusMessage;

}
