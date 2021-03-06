package io.kgu.chatservice.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestLoginDto {

    @NotNull(message = "userId 값이 필요합니다.")
    private String userId;

    @NotNull(message = "이메일이 존재하지 않습니다!")
    @Size(min = 2, message = "이메일은 최소 2자 이상이어야 합니다.")
    @Email
    private String email;
}
