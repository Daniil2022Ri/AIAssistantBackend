package com.example.AIAssistantBackend.controller;


import com.example.AIAssistantBackend.model.ChatRequest;
import com.example.AIAssistantBackend.model.ChatResponse;
import com.example.AIAssistantBackend.service.ChatHistoryService;
import com.example.AIAssistantBackend.service.ChatSessionService;
import com.example.AIAssistantBackend.service.OpenAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat API", description = "AI Assistant chat endpoints")
public class ChatController {

    private final OpenAiService openAiService;
    private final ChatSessionService chatSessionService;
    private final ChatHistoryService chatHistoryService;


    @PostMapping
    @Operation(summary = "Send message and get full response")
    public Mono<ChatResponse> chat(@RequestBody ChatRequest request) {
        String sessionId = getOrCreateSessionId(request.getSessionId());

        log.info("Processing chat request for session: {}", sessionId);

        return chatSessionService.getSessionContext(sessionId)
                .flatMap(context -> openAiService.chat(request.getMessage(), context))
                .flatMap(response -> {
                    // Сохраняем в историю
                    return chatHistoryService.saveMessage(sessionId, "user", request.getMessage())
                            .then(chatHistoryService.saveMessage(sessionId, "assistant", response.getContent()))
                            .then(chatSessionService.updateContext(sessionId, request.getMessage(), response.getContent()))
                            .thenReturn(response);
                });
    }


    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Send message and get streaming response (SSE)")
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        String sessionId = getOrCreateSessionId(request.getSessionId());

        log.info("Processing streaming request for session: {}", sessionId);

        return chatSessionService.getSessionContext(sessionId)
                .flatMapMany(context -> openAiService.chatStream(request.getMessage(), context))
                .doOnNext(chunk -> {
                    log.debug("Received chunk: {}", chunk);
                })
                .doOnComplete(() -> {
                    log.info("Stream completed for session: {}", sessionId);
                })
                .onErrorResume(e -> {
                    log.error("Stream error for session {}: {}", sessionId, e.getMessage());
                    return Flux.just("Error: " + e.getMessage());
                });
    }


    @GetMapping("/{sessionId}/history")
    @Operation(summary = "Get chat history by session ID")
    public Flux<ChatResponse> getHistory(@PathVariable String sessionId) {
        return chatHistoryService.getChatHistory(sessionId);
    }


    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete chat session")
    public Mono<Void> deleteSession(@PathVariable String sessionId) {
        return chatSessionService.deleteSession(sessionId)
                .then(chatHistoryService.deleteHistory(sessionId));
    }

    private String getOrCreateSessionId(String providedId) {
        return providedId != null ? providedId : UUID.randomUUID().toString();
    }
}