package com.grabit.config;

import com.grabit.handler.CustomAuthHandler;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http, CustomAuthHandler customAuthHandler) throws Exception {
        http.securityMatcher(
                "/oauth2/**",
                "/.well-known/**",
                "/connect/**"
        );
        http.oauth2AuthorizationServer(authServer->authServer
                .clientAuthentication(clientAuth->clientAuth
                        .errorResponseHandler(customAuthHandler)
                )
                .tokenEndpoint(tokenEndpoint->tokenEndpoint
                        .errorResponseHandler(customAuthHandler)
                )
        );

        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource){
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

//     Register Clients
    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        RegisteredClient orderServiceClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("order-service")
                        .clientSecret("{noop}order-secret")
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                        .scope("internal")
                        .tokenSettings(serviceTokenSettings())
                        .build();

        return new InMemoryRegisteredClientRepository(orderServiceClient);
    }

    // RSA Key for JWT signing
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // 1. Generate or load your RSA Key Pair
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // 2. Create a JWK object (the standard format for OAuth keys)
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        // 3. Put it in a Set and return it
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (selector, context) -> selector.select(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(4096);
            keyPair = generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public TokenSettings serviceTokenSettings(){
        return TokenSettings
                .builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .build();
    }

}
