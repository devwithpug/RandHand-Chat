package io.kgu.chatservice.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@NoArgsConstructor
public class MessageSyncDto implements Serializable {

    private String syncTime;
    private List<MessageDto> messages;

    public MessageSyncDto(LocalDateTime syncTime, List<MessageDto> messages) {
        this.syncTime = syncTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.messages = messages;
    }
}
