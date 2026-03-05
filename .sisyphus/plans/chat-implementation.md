# Chat Implementation Work Plan

## TL;DR

> **Quick Summary**: Implement a persistent chat system with dynamic tool calling (FunctionCall), SSE event streaming, and full-stack (Vue 3 + Spring Boot) integration.
> 
> **Deliverables**:
> - Backend: Chat session/entity models, ChatService with LangChain4J multi-provider support, SSE endpoints
> - Frontend: Chat UI components, SSE event handling, real-time display
> - Database: chat_session, chat_message, tool_call tables
> 
> **Estimated Effort**: Large
> **Parallel Execution**: YES - 4 waves
> **Critical Path**: Models → Service → Controller → Frontend

---

## Context

### Original Request
Implement Chat dialog functionality per `docs/chatimpl.md`:
1. Persistent chat storage (database)
2. Dynamic tool calling via LangChain4J FunctionCall
3. SSE event streaming for real-time updates
4. Vue 3 frontend for display

### User Preferences (Confirmed)
- **Test Strategy**: TDD (Red-Green-Refactor)
- **LLM Provider**: Multiple Providers (OpenAI, Gemini, etc.)
- **Scope**: Full Stack (Backend + Frontend)

### Existing Patterns Found

**LangChain4J Integration** (in autoflow-plugins):
- `LlmService.java` - Main AI service with ChatLanguageModel
- `ChatLanguageModelProvider` interface - Provider abstraction
- `OpenAiChatModelProvider`, `GeminiChatModelProvider` - Multiple implementations
- Already converts SPI Services to AI tools via `Function` wrapper

**SSE Implementation**:
- `ExecutionController.java:executeSSE()` - Existing SSE endpoint pattern
- `SSEContext.java` - SSE context management utilities
- `ServiceSSEListener.java` - Event emission pattern

**Database Models** (MyBatis-Flex):
- `@Table` annotation on model classes
- Convention: `WorkflowInst`, `ExecutionInst`, `ServiceEntity`
- Table naming: snake_case (e.g., `workflow_inst`)

**Frontend SSE**:
- `flowsse.ts` - SSE handling with EventSource
- Pattern: connect, onmessage, display events

---

## Work Objectives

### Core Objective
Implement persistent chat with dynamic tool calling and real-time SSE updates.

### Concrete Deliverables

| Component | File/Path |
|-----------|------------|
| Chat Session Model | `autoflow-app/.../model/ChatSession.java` |
| Chat Message Model | `autoflow-app/.../model/ChatMessage.java` |
| Tool Call Model | `autoflow-app/.../model/ToolCall.java` |
| Chat Service | `autoflow-app/.../service/ChatService.java` |
| Chat Controller | `autoflow-app/.../rest/ChatController.java` |
| Chat Store (Pinia) | `autoflow-fe/src/stores/chat.ts` |
| Chat View | `autoflow-fe/src/views/Chat/ChatView.vue` |
| Chat Components | `autoflow-fe/src/components/Chat/*` |

### Definition of Done

- [ ] Chat sessions persist across requests
- [ ] Messages stored with session_id, timestamp
- [ ] Tool calls recorded with parameters and results
- [ ] SSE streams tool_call events to frontend
- [ ] Frontend displays real-time conversation
- [ ] Multiple LLM providers supported
- [ ] All tests pass

### Must Have
- Database persistence for sessions, messages, tool calls
- LangChain4J FunctionCall with SPI Service tools
- SSE streaming for tool execution events
- Vue 3 UI with real-time updates
- TDD: tests pass before implementation

### Must NOT Have
- Hardcoded single LLM provider
- In-memory only storage (must persist)
- Blocking calls (use async reactive)
- Duplicate SSE infrastructure (reuse existing)

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES (JUnit 5 in pom.xml)
- **Automated tests**: TDD (Red-Green-Refactor)
- **Framework**: JUnit 5 + Mockito + Spring Boot Test
- **Pattern**: Each task includes failing test first, then minimal implementation

### QA Policy
Every task includes agent-executed QA scenarios:
- Backend: JUnit tests with @WebMvcTest / @DataJpaTest
- Integration: curl commands to verify endpoints
- Frontend: Playwright for browser verification

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Foundation - 5 tasks):
├── T1: Database Models - ChatSession, ChatMessage, ToolCall entities
├── T2: MyBatis-Flex Mappers - Repository layer
├── T3: Chat DTOs - Request/Response objects
├── T4: LLM Provider Integration - Multi-provider setup
└── T5: SPI Tool Adapter - Convert Services to LangChain4J Tools

Wave 2 (Core Backend - 4 tasks):
├── T6: ChatService - Business logic with LangChain4J
├── T7: ChatController - REST endpoints + SSE
├── T8: Event Models - ChatEvent, ToolCallEvent DTOs
└── T9: ChatSSEListener - Emit events to clients

Wave 3 (Frontend - 3 tasks):
├── T10: Chat Store - Pinia state management
├── T11: Chat API Client - Axios + SSE handling
└── T12: Chat Components - MessageList, Input, ToolCallDisplay

Wave 4 (Integration - 2 tasks):
├── T13: ChatView - Main page composing components
└── T14: Integration Test - End-to-end verification

Wave FINAL (Verification):
├── F1: Plan Compliance Audit
├── F2: Code Quality Review (checkstyle + tests)
└── F3: Real Manual QA
```

### Dependency Matrix
- **T1-T5**: — — (Wave 1, can run in parallel)
- **T6**: T1, T4, T5 — T7
- **T7**: T3, T6, T8, T9 — T14
- **T8**: T6 — T9
- **T9**: T8 — T14
- **T10-T12**: — — (Wave 3, can run in parallel)
- **T13**: T10, T11, T12 — F3
- **T14**: T7, T13 — F3

---

## TODOs

- [ ] 1. **Database Models (ChatSession, ChatMessage, ToolCall)**

  **What to do**:
  - Create `ChatSession.java` with fields: id, title, createdAt, updatedAt, status
  - Create `ChatMessage.java` with fields: id, sessionId, role (user/assistant/system), content, createdAt
  - Create `ToolCall.java` with fields: id, messageId, toolName, parameters (JSON), result, status, createdAt
  - Use MyBatis-Flex `@Table` annotation matching existing conventions

  **Must NOT do**:
  - Don't add fields not in requirements
  - Don't use different ORM (must use MyBatis-Flex)

  **Recommended Agent Profile**:
  - **Category**: `quick` - Straightforward model creation following existing patterns
  - **Skills**: None required - standard model work
  
  **Parallelization**:
  - **Can Run In Parallel**: YES (with T2-T5)
  - **Parallel Group**: Wave 1
  - **Blocks**: T6 (ChatService)
  - **Blocked By**: None

  **References**:
  - `autoflow-app/.../model/WorkflowInst.java` - Example of @Table usage
  - `autoflow-app/.../model/ExecutionInst.java` - Another model example

  **Acceptance Criteria**:
  - [ ] Test file created: src/test/java/.../model/ChatSessionTest.java
  - [ ] Entities compile and pass checkstyle

  **QA Scenarios**:
  ```
  Scenario: Verify model compilation
    Tool: Bash
    Preconditions: None
    Steps:
      1. mvn compile -pl autoflow-app
    Expected Result: BUILD SUCCESS
    Failure Indicators: Compilation errors
  ```

- [ ] 2. **MyBatis-Flex Mappers**

  **What to do**:
  - Create `ChatSessionMapper.java` - extends BaseMapper<ChatSession>
  - Create `ChatMessageMapper.java` - extends BaseMapper<ChatMessage>
  - Create `ToolCallMapper.java` - extends BaseMapper<ToolCall>
  - Ensure proper ID generation strategies

  **Must NOT do**:
  - Don't write custom SQL (use BaseMapper capabilities)

  **Recommended Agent Profile**:
  - **Category**: `quick` - Standard mapper pattern
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES (with T1, T3-T5)
  - **Parallel Group**: Wave 1
  - **Blocks**: T6
  - **Blocked By**: T1

  **References**:
  - `autoflow-app/.../mapper/WorkflowMapper.java` - Existing mapper pattern

- [ ] 3. **Chat DTOs**

  **What to do**:
  - Create `ChatRequest.java` - sessionId, message, provider preference
  - Create `ChatResponse.java` - message, toolCalls, sessionId
  - Create `SendMessageRequest.java` - for POST /chat/message

  **Recommended Agent Profile**:
  - **Category**: `quick` - Simple DTOs

  **Parallelization**:
  - **Can Run In Parallel**: YES (with T1-T2, T4-T5)

  **References**:
  - `autoflow-plugins/autoflow-llm/src/main/java/.../LlmParameter.java` - Parameter pattern

- [ ] 4. **LLM Provider Integration**

  **What to do**:
  - Extend existing `ChatLanguageModelProvider` interface if needed
  - Ensure multiple providers work with ChatLanguageModel
  - Create `ChatModelFactory` to select provider based on config

  **Must NOT do**:
  - Don't duplicate existing provider implementations

  **References**:
  - `autoflow-plugins/autoflow-llm/.../OpenAiChatModelProvider.java`
  - `autoflow-plugins/autoflow-gemini/.../GeminiChatModelProvider.java`

- [ ] 5. **SPI Tool Adapter**

  **What to do**:
  - Create `ServiceToolFactory` - converts SPI Services to LangChain4J Tools
  - Use existing `Function` wrapper pattern from LlmService
  - Support dynamic tool selection based on user input

  **References**:
  - `autoflow-plugins/autoflow-llm/.../LlmService.java` - Function wrapper pattern
  - `autoflow-spi/.../Service.java` - SPI Service interface

- [ ] 6. **ChatService**

  **What to do**:
  - Implement `sendMessage(sessionId, userMessage)` - saves message, calls LLM
  - Implement `executeToolCall(toolName, params)` - runs SPI Service
  - Implement `streamEvents(sessionId)` - returns SSE emitter
  - Use LangChain4J ChatLanguageModel for responses
  - Persist all messages and tool calls to database

  **Recommended Agent Profile**:
  - **Category**: `deep` - Complex business logic

  **References**:
  - `autoflow-plugins/autoflow-llm/.../LlmService.java` - LLM usage

- [ ] 7. **ChatController**

  **What to do**:
  - POST /api/chat/sessions - create new session
  - GET /api/chat/sessions - list sessions
  - GET /api/chat/sessions/{id}/messages - get session messages
  - POST /api/chat/sessions/{id}/messages - send message (returns SSE)

  **References**:
  - `autoflow-app/.../rest/ExecutionController.java` - SSE endpoint pattern

- [ ] 8. **Chat Event DTOs**

  **What to do**:
  - Create `ChatEvent` - base event with type, timestamp
  - Create `MessageEvent` - extends ChatEvent for messages
  - Create `ToolCallEvent` - extends ChatEvent for tool executions
  - Create `ErrorEvent` - for error handling

- [ ] 9. **ChatSSEListener**

  **What to do**:
  - Create listener that emits events during tool execution
  - Reuse existing `SSEContext` pattern
  - Ensure events include tool parameters and results

  **References**:
  - `autoflow-app/.../listener/ServiceSSEListener.java`

- [ ] 10. **Chat Store (Pinia)**

  **What to do**:
  - Create `useChatStore` - manages chat state
  - Store: sessions, currentSessionId, messages
  - Actions: createSession, sendMessage, loadMessages

- [ ] 11. **Chat API Client**

  **What to do**:
  - Create `chatApi.ts` - Axios client for chat endpoints
  - Implement SSE connection handling
  - Parse SSE events and emit to store

  **References**:
  - `autoflow-fe/src/views/FlowDesigner/flowsse.ts` - SSE handling

- [ ] 12. **Chat Components**

  **What to do**:
  - `MessageList.vue` - displays messages with roles
  - `MessageItem.vue` - single message with markdown
  - `ToolCallDisplay.vue` - shows tool calls and results
  - `ChatInput.vue` - message input with send button

- [ ] 13. **ChatView**

  **What to do**:
  - Create `ChatView.vue` - main chat page
  - Compose MessageList, ChatInput, ToolCallDisplay
  - Handle session creation and switching

- [ ] 14. **Integration Test**

  **What to do**:
  - End-to-end test: create session, send message, verify response
  - Verify tool calls are recorded
  - Verify SSE events are received

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — Verify all deliverables present
- [ ] F2. **Code Quality Review** — mvn validate + tests pass
- [ ] F3. **Real Manual QA** — Full flow test with browser

---

## Commit Strategy

- Wave 1: `feat(chat): add database models and mappers`
- Wave 2: `feat(chat): add service and controller with SSE`
- Wave 3: `feat(chat): add frontend chat components`
- Wave 4: `feat(chat): add integration and full flow`

---

## Success Criteria

- [ ] POST /api/chat/sessions creates session in DB
- [ ] Messages persist with session_id
- [ ] Tool calls stored with parameters and results
- [ ] SSE endpoint streams events to frontend
- [ ] Multiple LLM providers configurable
- [ ] All checkstyle and tests pass
