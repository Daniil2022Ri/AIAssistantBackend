package com.example.AIAssistantBackend.service;


import com.example.AIAssistantBackend.model.ChatResponse;
import com.example.AIAssistantBackend.model.Message;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryService {

    private final DatabaseClient databaseClient;

    private static final BiFunction<Row, RowMetadata, ChatResponse> MAPPER = (row, meta) ->
            ChatResponse.builder()
                    .content(row.get("content", String.class))
                    .timestamp(row.get("created_at", Instant.class))
                    .build();

    /**
     * Сохранить сообщение в историю
     */
    public Mono<Void> saveMessage(String sessionId, String role, String content) {
        String sql = """
            INSERT INTO chat_messages (session_id, role, content, created_at)
            VALUES (:sessionId, :role, :content, NOW())
            """;

        return databaseClient.sql(sql)
                .bind("sessionId", sessionId)
                .bind("role", role)
                .bind("content", content)
                .fetch()
                .rowsUpdated()
                .doOnNext(count -> log.debug("Saved {} message for session {}", role, sessionId))
                .then();
    }

    /**
     * Получить историю чата
     */
    public Flux<ChatResponse> getChatHistory(String sessionId) {
        String sql = """
            SELECT content, created_at 
            FROM chat_messages 
            WHERE session_id = :sessionId 
            ORDER BY created_at ASC
            """;

        return databaseClient.sql(sql)
                .bind("sessionId", sessionId)
                .map(MAPPER)
                .all();
    }

    /**
     * Удалить историю
     */
    public Mono<Void> deleteHistory(String sessionId) {
        String sql = "DELETE FROM chat_messages WHERE session_id = :sessionId";

        return databaseClient.sql(sql)
                .bind("sessionId", sessionId)
                .fetch()
                .rowsUpdated()
                .then();
    }
}
