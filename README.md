# 🤖 AIAssistantBackend

Reactive AI Assistant Backend built with **Spring Boot 3**, **WebFlux**, **OpenAI API**, and **Redis**.

A scalable backend service providing conversational AI capabilities with session management, chat history persistence, and reactive streaming architecture.

---

## 🚀 Features

* 🤖 OpenAI API Integration
* ⚡ Reactive Programming with Spring WebFlux
* 🧠 Chat Session Management
* 💾 Redis-based Conversation Storage
* 🔄 Conversation Context Preservation
* 📡 Non-blocking Request Processing
* 🏗 Clean Service-Oriented Architecture
* 🐳 Docker Support

---

## 🏛 Architecture

```text
Client
   │
   ▼
ChatController
   │
   ▼
OpenAiService
   │
   ├── OpenAI API
   │
   ▼
ChatSessionService
   │
   ▼
ChatHistoryService
   │
   ▼
Redis
```

---

## 🛠 Tech Stack

### Backend

* Java 21
* Spring Boot 3
* Spring WebFlux

### AI

* OpenAI API

### Storage

* Redis

### Infrastructure

* Docker
* Maven

---

## 📂 Project Structure

```text
controller/
 └── ChatController

service/
 ├── OpenAiService
 ├── ChatSessionService
 └── ChatHistoryService

config/
 ├── OpenAiConfig
 ├── RedisConfig
 └── WebFluxConfig

model/
 ├── ChatRequest
 ├── ChatResponse
 ├── ChatSession
 └── Message
```

---

## 🔄 Request Flow

1. User sends a prompt.
2. Controller validates request.
3. Session service loads chat history.
4. OpenAiService sends context to OpenAI.
5. Response is received asynchronously.
6. Conversation is stored in Redis.
7. Result is returned to the client.

---

## ⚙️ Running Locally

### Clone

```bash
git clone https://github.com/Daniil2022Ri/AIAssistantBackend.git
cd AIAssistantBackend
```

### Start Redis

```bash
docker compose up -d
```

### Run Application

```bash
./mvnw spring-boot:run
```

---

## 🔧 Configuration

application.yml

```yaml
openai:
  api-key: YOUR_API_KEY

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

## 🎯 Key Concepts

* Reactive Programming
* Session Persistence
* Conversation Context Management
* External AI Service Integration
* Non-Blocking Architecture

---

## 📈 Future Improvements

* SSE Streaming Responses
* Vector Database Integration
* RAG Architecture
* User Authentication
* Rate Limiting
* Multi-Model Support

---

## 👨‍💻 Author

Daniil Rybiakov

Java Backend / FullStack Engineer

Microservices • Event-Driven Architecture • Distributed Systems
