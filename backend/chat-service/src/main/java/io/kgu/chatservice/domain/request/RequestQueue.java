package io.kgu.chatservice.domain.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RequestQueue {

    @NotNull
    @NotBlank
    private String userId;

    @NotNull
    @NotBlank
    private String gesture;

}
