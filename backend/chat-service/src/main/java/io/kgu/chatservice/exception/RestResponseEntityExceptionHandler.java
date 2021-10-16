package io.kgu.chatservice.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected Map<String, String> handleException(Exception ex, HttpServletResponse resp) {

        if (ex instanceof EntityNotFoundException) resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else if (ex instanceof UsernameNotFoundException) resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else if (ex instanceof IllegalArgumentException) resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if (ex instanceof DuplicateKeyException) resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else if (ex instanceof IOException) resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        return trimErrorMessage(ex.getMessage());
    }

    private Map<String, String> trimErrorMessage(String msg) {

        if (msg == null) {
            return new HashMap<>();
        }

        String[] error = msg.split("\'");
        error[0] = error[0].substring(0, error[0].length()-1);

        return Map.of(error[0], error[1]);
    }

}
