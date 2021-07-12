package io.kgu.userservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {

    private String userId;
    private String auth;
    private String email;
    private String name;
    private String statusMessage;
    private String picture;
    private LocalDateTime createdAt;

}
