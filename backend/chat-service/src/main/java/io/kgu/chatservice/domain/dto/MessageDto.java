package io.kgu.chatservice.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDto {

    private String sessionId;
    private String fromUser;
    private String toUser;
    private String content;

}
