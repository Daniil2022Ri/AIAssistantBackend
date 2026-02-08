package com.example.AIAssistantBackend.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChatResponse {
    private String content;
    private String model;
    private Integer tokensUsed;
    private Instant timestamp;
}
