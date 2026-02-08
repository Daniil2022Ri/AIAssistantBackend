package com.example.AIAssistantBackend.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ChatSession {
    private String id;
    private List<Message> messages;
    private Instant createdAt;
    private Instant lastActivity;
}
