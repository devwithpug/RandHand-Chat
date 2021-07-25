package io.kgu.chatservice.domain.response;

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
public class ResponseUser implements Serializable {

    private String userId;
    private String email;
    private String name;
    private String statusMessage;
    private String picture;
    private List<ResponseUser> userFriends;
    private List<ResponseUser> userBlocked;

}
