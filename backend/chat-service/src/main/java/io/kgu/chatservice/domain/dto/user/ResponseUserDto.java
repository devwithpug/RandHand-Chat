package io.kgu.chatservice.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUserDto implements Serializable {

    private String userId;
    private String email;
    private String name;
    private String statusMessage;
    private String picture;
    private List<ResponseUserDto> userFriends;
    private List<ResponseUserDto> userBlocked;

}
