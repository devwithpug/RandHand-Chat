package io.kgu.userservice.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {

    private String userId;
    private String email;
    private String name;
    private String statusMessage;
    private String picture;
    private LocalDateTime createdAt;

}
