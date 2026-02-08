package com.example.AIAssistantBackend.service;

import com.example.AIAssistantBackend.model.ChatResponse;
import com.example.AIAssistantBackend.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final WebClient openAiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.max-tokens}")
    private int maxTokens;

    @Value("${openai.api.temperature}")
    private double temperature;


    public Mono<ChatResponse> chat(String userMessage, List<Message> context) {
        ObjectNode requestBody = buildRequestBody(userMessage, context, false);

        log.debug("Sending request to OpenAI: {}", requestBody);

        return openAiWebClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::extractResponse)
                .doOnNext(response -> log.debug("Received response: {}", response.getContent()));
    }


    public Flux<String> chatStream(String userMessage, List<Message> context) {
        ObjectNode requestBody = buildRequestBody(userMessage, context, true);

        log.debug("Sending streaming request to OpenAI");

        return openAiWebClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring(5).trim())
                .filter(data -> !data.equals("[DONE]"))
                .flatMap(this::extractStreamChunk)
                .onErrorResume(e -> {
                    log.error("OpenAI streaming error: {}", e.getMessage());
                    return Flux.just("Error: " + e.getMessage());
                });
    }

    private ObjectNode buildRequestBody(String userMessage, List<Message> context, boolean stream) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", model);
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);
        request.put("stream", stream);

        ArrayNode messages = request.putArray("messages");

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a helpful AI assistant. Be concise and clear.");

        if (context != null) {
            for (Message msg : context) {
                ObjectNode msgNode = messages.addObject();
                msgNode.put("role", msg.getRole());
                msgNode.put("content", msg.getContent());
            }
        }

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        return request;
    }

    private ChatResponse extractResponse(JsonNode response) {
        String content = response.path("choices").get(0)
                .path("message")
                .path("content")
                .asText();

        String model = response.path("model").asText();
        int tokens = response.path("usage").path("total_tokens").asInt();

        return ChatResponse.builder()
                .content(content)
                .model(model)
                .tokensUsed(tokens)
                .build();
    }

    private Flux<String> extractStreamChunk(String data) {
        try {
            JsonNode node = objectMapper.readTree(data);
            String content = node.path("choices").get(0)
                    .path("delta")
                    .path("content")
                    .asText();

            if (!content.isEmpty()) {
                return Flux.just(content);
            }
        } catch (Exception e) {
            log.debug("Could not extract chunk: {}", data);
        }
        return Flux.empty();
    }
}
