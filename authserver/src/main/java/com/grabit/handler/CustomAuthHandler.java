package com.grabit.handler;

import com.grabit.Utilities.Utility;
import com.grabit.exception.APIError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.swing.text.Utilities;
import java.io.IOException;
import java.util.Map;

@Component
@Log4j2
public class CustomAuthHandler implements AuthenticationFailureHandler, AuthenticationEntryPoint {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        APIError apiError;
        log.error(exception);
        int httpStatus=401;
        if(exception instanceof OAuth2AuthenticationException e){
            apiError=new APIError(e.getError().getErrorCode(),e.getError().getDescription());
            switch (e.getError().getErrorCode()){
                case "invalid_client":{
                    apiError=new APIError(e.getError().getErrorCode(),"Client authentication failed");
                    break;
                }
                case "invalid_scope":{
                    httpStatus=HttpServletResponse.SC_FORBIDDEN;
                    apiError=new APIError(e.getError().getErrorCode(),"The provided scope is not valid");
                    break;
                }
                default:{
                    httpStatus=HttpServletResponse.SC_BAD_REQUEST;
                }
            }
        }
        else
            apiError=new APIError("authentication_error", exception.getMessage());
        response.setStatus(httpStatus);
        new ObjectMapper().writeValue(response.getWriter(),apiError);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error(authException);
    }
}
