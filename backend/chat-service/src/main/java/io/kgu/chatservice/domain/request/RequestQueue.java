package io.kgu.chatservice.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestQueue {

    @NotNull
    @NotBlank
    private String userId;

    @NotNull
    @NotBlank
    private String gesture;

}
