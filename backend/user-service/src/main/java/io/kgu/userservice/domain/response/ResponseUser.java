package io.kgu.userservice.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {

    private String userId;
    private String email;
    private String name;
    private String statusMessage;
    private String picture;
    private List<ResponseUser> userFriends;
    private List<ResponseUser> userBlocked;

}
