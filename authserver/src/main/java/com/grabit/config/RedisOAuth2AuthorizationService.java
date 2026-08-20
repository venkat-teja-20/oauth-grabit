package com.grabit.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;

@Service
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final String AUTH_KEY_PREFIX="auth:";
    private final String TOKEN_KEY_PREFIX="token:";

    private final RedisTemplate<String, OAuth2Authorization> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;

    public RedisOAuth2AuthorizationService(RedisTemplate<String, OAuth2Authorization> redisTemplate, RedisTemplate<String, String> stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Duration ttl=resolveMaxTtl(authorization);
        String authKey= AUTH_KEY_PREFIX+authorization.getId();
        redisTemplate.opsForValue().set(authKey,authorization,ttl);

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authCode= authorization.getToken(OAuth2AuthorizationCode.class);
        if(authCode!=null){
            stringRedisTemplate.opsForValue().set(TOKEN_KEY_PREFIX+authCode.getToken().getTokenValue(),authorization.getId(),ttl);
        }

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken= authorization.getAccessToken();
        if(accessToken!=null){
            stringRedisTemplate.opsForValue().set(TOKEN_KEY_PREFIX+accessToken.getToken().getTokenValue(),authorization.getId(),ttl);
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken=authorization.getRefreshToken();
        if(refreshToken!=null){
            stringRedisTemplate.opsForValue().set(TOKEN_KEY_PREFIX+refreshToken.getToken().getTokenValue(),authorization.getId(),ttl);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        String authKey=AUTH_KEY_PREFIX+authorization.getId();
        redisTemplate.delete(authKey);

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authCode= authorization.getToken(OAuth2AuthorizationCode.class);
        if(authCode!=null){
            stringRedisTemplate.delete(TOKEN_KEY_PREFIX+authCode.getToken().getTokenValue());
        }

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken= authorization.getAccessToken();
        if(accessToken!=null){
            stringRedisTemplate.delete(TOKEN_KEY_PREFIX+accessToken.getToken().getTokenValue());
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken=authorization.getRefreshToken();
        if(refreshToken!=null){
            stringRedisTemplate.delete(TOKEN_KEY_PREFIX+refreshToken.getToken().getTokenValue());
        }
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return redisTemplate.opsForValue().get(AUTH_KEY_PREFIX+id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        String id=stringRedisTemplate.opsForValue().get(TOKEN_KEY_PREFIX+token);
        if(id==null)
            return null;
        return findById(id);
    }

    private Duration resolveMaxTtl(OAuth2Authorization authorization) {
        Instant now = Instant.now();
        Instant latest = now.plusSeconds(120); // fallback: 2 min for auth codes

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken != null && accessToken.getToken().getExpiresAt() != null) {
            if (accessToken.getToken().getExpiresAt().isAfter(latest)) {
                latest = accessToken.getToken().getExpiresAt();
            }
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        if (refreshToken != null && refreshToken.getToken().getExpiresAt() != null) {
            if (refreshToken.getToken().getExpiresAt().isAfter(latest)) {
                latest = refreshToken.getToken().getExpiresAt();
            }
        }

        return Duration.between(now, latest);
    }
}

