package io.kgu.chatservice.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    protected Map<String, String> handleConflict(ResponseStatusException ex, HttpServletResponse resp) {

        resp.setStatus(ex.getRawStatusCode());

        return trimErrorMessage(ex.getMessage());
    }

    private Map<String, String> trimErrorMessage(String msg) {

        if (msg == null) {
            return new HashMap<>();
        }

        String[] error = msg.split("\"");
        error[0] = error[0].substring(0, error[0].length()-1);

        return Map.of(error[0], error[1]);
    }

}
