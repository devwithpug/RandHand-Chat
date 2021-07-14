package io.kgu.chatservice.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class ChatDto implements Serializable {

    private String sessionId;
    private List<String> userIds;

}
