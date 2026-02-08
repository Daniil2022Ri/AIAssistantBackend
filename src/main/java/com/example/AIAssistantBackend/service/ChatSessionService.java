package com.example.AIAssistantBackend.service;


import com.example.AIAssistantBackend.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration SESSION_TTL = Duration.ofHours(1);
    private static final int MAX_CONTEXT_MESSAGES = 10;
    private static final String KEY_PREFIX = "chat:session:";


    public Mono<List<Message>> getSessionContext(String sessionId) {
        String key = KEY_PREFIX + sessionId;

        return redisTemplate.opsForValue().get(key)
                .flatMap(json -> {
                    try {
                        List<Message> messages = objectMapper.readValue(json,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class));
                        return Mono.just(messages);
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing session context: {}", e.getMessage());
                        return Mono.just(new ArrayList<Message>());
                    }
                })
                .defaultIfEmpty(new ArrayList<>());
    }


    public Mono<Boolean> updateContext(String sessionId, String userMessage, String assistantResponse) {
        String key = KEY_PREFIX + sessionId;

        return getSessionContext(sessionId)
                .flatMap(messages -> {

                    messages.add(new Message("user", userMessage, System.currentTimeMillis()));
                    messages.add(new Message("assistant", assistantResponse, System.currentTimeMillis()));


                    if (messages.size() > MAX_CONTEXT_MESSAGES) {
                        messages = messages.subList(messages.size() - MAX_CONTEXT_MESSAGES, messages.size());
                    }

                    try {
                        String json = objectMapper.writeValueAsString(messages);
                        return redisTemplate.opsForValue().set(key, json, SESSION_TTL);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing session context: {}", e.getMessage());
                        return Mono.just(false);
                    }
                });
    }


    public Mono<Boolean> deleteSession(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        return redisTemplate.delete(key).map(deleted -> deleted > 0);
    }
}