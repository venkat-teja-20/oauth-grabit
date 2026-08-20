package com.grabit.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        BasicPolymorphicTypeValidator.Builder typeValidatorBuilder = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class);

        return JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(getClass().getClassLoader(), typeValidatorBuilder))
                .addModule(new OAuth2AuthorizationServerJacksonModule())
                .activateDefaultTyping(
                        typeValidatorBuilder.build(),
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                )
                .build();
    }

    @Bean
    public RedisTemplate<String, OAuth2Authorization> redisTemplate(RedisConnectionFactory connectionFactory,
                                                                     ObjectMapper redisObjectMapper) {
        RedisSerializer<OAuth2Authorization> serializer = new RedisSerializer<>() {
            @Override
            public byte[] serialize(OAuth2Authorization value) throws SerializationException {
                if (value == null) return null;
                try {
                    return redisObjectMapper.writeValueAsBytes(value);
                } catch (JacksonException e) {
                    throw new SerializationException("Failed to serialize OAuth2Authorization", e);
                }
            }

            @Override
            public OAuth2Authorization deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null) return null;
                try {
                    return redisObjectMapper.readValue(bytes, OAuth2Authorization.class);
                } catch (JacksonException e) {
                    throw new SerializationException("Failed to deserialize OAuth2Authorization", e);
                }
            }
        };

        RedisTemplate<String, OAuth2Authorization> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        return template;
    }
}
