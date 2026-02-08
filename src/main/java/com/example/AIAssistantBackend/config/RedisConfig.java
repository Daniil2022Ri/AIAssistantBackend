package com.example.AIAssistantBackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<String> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, String.class);

        RedisSerializationContext<String, String> serializationContext =
                RedisSerializationContext
                        .<String, String>newSerializationContext(new StringRedisSerializer())
                        .value(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}