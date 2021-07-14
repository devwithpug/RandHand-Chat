package io.kgu.chatservice.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class ResponseQueue implements Serializable {

    private String userId;

}
