package com.grabit.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Log4j2
public class APIExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity handleException(HttpServletRequest request,Exception e){
        if(request.getRequestURI().contains("/login/oauth2/code/mobile-client"))
            return ResponseEntity.status(200).body(null);
        log.error(e);
        return ResponseEntity
                .status(500)
                .body(Map.of("message",e.getMessage()));
    }
}
