package io.kgu.userservice.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {

    private String userId;
    private String auth;
    private String email;
    private String name;
    private String statusMessage;
    private String picture;
    private LocalDateTime createdAt;

}
