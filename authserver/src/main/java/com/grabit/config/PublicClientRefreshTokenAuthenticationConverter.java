package com.grabit.config;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;

public class PublicClientRefreshTokenAuthenticationConverter implements AuthenticationConverter {
    @Override
    public @Nullable Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if(!grantType.equals(AuthorizationGrantType.REFRESH_TOKEN.getValue()))
            return null;

        String clientId=request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if(clientId==null)
            return null;

        return new PublicClientRefreshTokenAuthentication(clientId);
    }
}
