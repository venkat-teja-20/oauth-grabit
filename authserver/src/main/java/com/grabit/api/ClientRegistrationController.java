package com.grabit.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/clients")
public class ClientRegistrationController {

    private final RegisteredClientRepository registeredClientRepository;

    public ClientRegistrationController(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @PostMapping
    public ResponseEntity<?> registerClient(@RequestBody ClientRegistrationRequest request) {
        RegisteredClient existing = registeredClientRepository.findByClientId(request.clientId());
        if (existing != null) {
            return ResponseEntity.status(HttpStatusCode.valueOf(409)).body(Map.of("error", "Client already exists: " + request.clientId()));
        }

        RegisteredClient.Builder builder = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(request.clientId());

        if (request.clientSecret() != null && !request.clientSecret().isBlank()) {
            builder.clientSecret("{noop}" + request.clientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);
        } else {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
        }

        request.grantTypes().forEach(grant ->
                builder.authorizationGrantType(new AuthorizationGrantType(grant)));

        request.scopes().forEach(builder::scope);

        if (request.redirectUris() != null) {
            request.redirectUris().forEach(builder::redirectUri);
        }

        builder.clientSettings(ClientSettings.builder()
                .requireProofKey(request.requirePkce())
                .requireAuthorizationConsent(false)
                .build());

        builder.tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(request.accessTokenTTLMinutes()))
                .refreshTokenTimeToLive(Duration.ofDays(request.refreshTokenTTLDays()))
                .reuseRefreshTokens(false)
                .build());

        registeredClientRepository.save(builder.build());

        return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(Map.of("message", "Client registered successfully", "clientId", request.clientId()));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClient(@PathVariable String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Client not found: " + clientId));
        }
        return ResponseEntity.ok(client);
    }
}
