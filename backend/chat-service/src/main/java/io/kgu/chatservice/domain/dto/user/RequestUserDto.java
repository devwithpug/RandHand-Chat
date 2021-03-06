package io.kgu.chatservice.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestUserDto implements Serializable {

    private String userId;

    @NotNull(message = "OAuth2 인증 서버가 존재하지 않습니다!")
    private String auth;

    @NotNull(message = "이메일이 존재하지 않습니다!")
    @Size(min = 2, message = "이메일은 최소 2자 이상이어야 합니다.")
    @Email(message = "이메일 형식이 맞지 않습니다.")
    private String email;

    @NotNull(message = "이름이 존재하지 않습니다!")
    @NotBlank(message = "이름이 비어있습니다.")
    private String name;

    @URL(message = "프로필 이미지 형식이 맞지 않습니다.")
    private String picture;

    @Size(max = 50, message = "상태 메시지는 최대 50자 입니다.")
    private String statusMessage;

}
