package com.grabit.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@RestControllerAdvice
@Log4j2
public class APIExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity handleException(HttpServletRequest request,Exception e){
        log.error(e);
        return ResponseEntity
                .status(500)
                .body(Map.of("message",e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleNoResourceFound(NoResourceFoundException ex) {
        // Intentionally empty
    }
}
