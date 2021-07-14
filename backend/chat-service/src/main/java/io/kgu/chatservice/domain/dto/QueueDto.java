package io.kgu.chatservice.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueueDto implements Serializable {

    private String userId;
    private String gesture;

}
