package com.example.AIAssistantBackend.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRequest {
    private String message;
    private String sessionId;
}