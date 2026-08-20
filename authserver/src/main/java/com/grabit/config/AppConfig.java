package com.grabit.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.grabit.mapper.MemberURLMapper;
import com.grabit.mapper.PartnerURLMapperBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
public class AppConfig {
    @Bean(name = "memberURLMapper")
    public MemberURLMapper memberURLMapper(){
        return PartnerURLMapperBuilder.builder()
                .routerURL(System.getenv("member_url"))
                .build();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    /* TODO:
        1. Check how to add Registered Client to DB in a different way
        2. Implement RBAC for APIs
        3. Add the special ROLE/Authority to OAUTH Client to fetch member password
    */
//    @Bean
//    public CommandLineRunner seedClients(RegisteredClientRepository registeredClientRepository){
//        return args -> {
//            RegisteredClient orderClient=RegisteredClient
//                    .withId(UUID.randomUUID().toString())
//                    .clientId("order-client")
//                    .clientSecret("{noop}order-secret")
//                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
//                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
//                    .scope("internal")
//                    .tokenSettings(serviceTokenSettings())
//                    .build();
//            RegisteredClient mobileClient=RegisteredClient
//                    .withId(UUID.randomUUID().toString())
//                    .clientId("mobile-client")
//                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
//                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//                    .redirectUri("http://127.0.0.1:9090/callback")
//                    .clientSettings(
//                            ClientSettings.builder()
//                                    .requireProofKey(true)
//                                    .requireAuthorizationConsent(false)
//                                    .build()
//                    )
//                    .scope("member")
//                    .scope("offline_access")
//                    .tokenSettings(appTokenSettings())
//                    .build();
//            registeredClientRepository.save(orderClient);
//            registeredClientRepository.save(mobileClient);
//        };
//    }
//
//    @Bean
//    public TokenSettings appTokenSettings(){
//        return TokenSettings
//                .builder()
//                .accessTokenTimeToLive(Duration.ofMinutes(10))
//                .refreshTokenTimeToLive(Duration.ofDays(30))
//                .reuseRefreshTokens(false)
//                .build();
//    }
//
//    @Bean
//    public TokenSettings serviceTokenSettings(){
//        return TokenSettings
//                .builder()
//                .accessTokenTimeToLive(Duration.ofMinutes(5))
//                .build();
//    }
}
