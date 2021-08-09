package io.kgu.chatservice.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
public class ChatDto implements Serializable {

    private String sessionId;
    private List<String> userIds;

    @ConstructorProperties({"sessionId", "userIds"})
    public ChatDto(String sessionId, List<String> userIds) {
        this.sessionId = sessionId;
        this.userIds = userIds;
    }
}
