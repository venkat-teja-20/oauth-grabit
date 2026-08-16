package com.grabit.config;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public class PublicClientRefreshProvider implements AuthenticationProvider {

    private final RegisteredClientRepository registeredClientRepository;

    public PublicClientRefreshProvider(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PublicClientRefreshTokenAuthentication publicClientRefreshTokenAuthentication=(PublicClientRefreshTokenAuthentication) authentication;

        if(!ClientAuthenticationMethod.NONE.equals(publicClientRefreshTokenAuthentication.getClientAuthenticationMethod()))
            return null;

        String clientId=publicClientRefreshTokenAuthentication.getPrincipal().toString();
        RegisteredClient registeredClient=registeredClientRepository.findByClientId(clientId);
        if(registeredClient==null){
            throw new OAuth2AuthenticationException(
                    OAuth2ErrorCodes.INVALID_CLIENT
            );

        }

        if(!registeredClient.getClientAuthenticationMethods().contains(publicClientRefreshTokenAuthentication.getClientAuthenticationMethod())){
            throw new OAuth2AuthenticationException("Invalid Authentication Method");
        }
        return new PublicClientRefreshTokenAuthentication(registeredClient);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PublicClientRefreshTokenAuthentication.class.isAssignableFrom(authentication);
    }
}
