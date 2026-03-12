package com.grabit.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

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
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, OAuth2TokenGenerator<?> tokenGenerator) throws Exception {

        // 1. Instantiate the configurer directly
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                // 2. ONLY trigger this filter chain for Auth Server endpoints
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                // 3. Apply the authorization server defaults
                .with(authorizationServerConfigurer, customizer -> customizer
                        .tokenGenerator(tokenGenerator))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                // 4. Redirect to login page for unauthorized requests
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                );

        return http.build();
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JWKSource<SecurityContext> jwkSource) {
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        OAuth2TokenGenerator<OAuth2RefreshToken> refreshTokenGenerator = new CustomOAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator,
                refreshTokenGenerator
        );
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

        RegisteredClient mobileClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("mobile-client")
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                        .redirectUri("http://localhost:8081/callback")
                        .scope("member")
                        .scope("offline_access")
                        .clientSettings(
                                ClientSettings.builder()
                                        .requireProofKey(true)   // PKCE REQUIRED
                                        .requireAuthorizationConsent(false)
                                        .build()
                        )
                        .tokenSettings(mobileTokenSettings())
                        .build();

        return new InMemoryRegisteredClientRepository(orderServiceClient,mobileClient);
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

    @Bean
    public TokenSettings mobileTokenSettings(){
        return TokenSettings
                .builder()
                .accessTokenTimeToLive(Duration.ofMinutes(10))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(false)
                .build();
    }

}
