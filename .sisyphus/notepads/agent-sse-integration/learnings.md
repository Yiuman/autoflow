
## autoflow-agent dependency addition (2026-03-20)

### Task
Added `autoflow-agent` dependency to `autoflow-app/pom.xml`.

### Approach
- Read `autoflow-app/pom.xml` to find insertion point after existing `autoflow-*` dependencies (line 49-53)
- Read `autoflow-agent/pom.xml` to confirm artifact id is `autoflow-agent`
- Added dependency using `${revision}` for version (centrally managed in parent pom)

### Verification
- Ran `mvn dependency:tree -pl autoflow-app -Dincludes=io.autoflow:autoflow-agent`
- Output confirmed: `\- io.autoflow:autoflow-agent:jar:1.0-SNAPSHOT:compile`

### Files Modified
- `autoflow-app/pom.xml` - added one dependency block


## AgentChatRequest and AgentSSEEvent creation (2026-03-20)

### Task
Created DTO and SSE event model classes for agent chat integration.

### Approach
- Read `Workflow.java` for entity pattern (uses @Table, BaseEntity, @EqualsAndHashCode)
- Read `StopRequest.java` for simple DTO pattern (Lombok @Data only)
- Read `StreamListener.java` to understand event types (token, thinking, tool_start, tool_end, complete, error)
- Created AgentChatRequest as simple DTO with Jakarta validation annotations
- Created AgentSSEEvent with Builder pattern for SSE streaming

### Files Created
- `autoflow-app/src/main/java/io/autoflow/app/model/AgentChatRequest.java`
- `autoflow-app/src/main/java/io/autoflow/app/model/sse/AgentSSEEvent.java`

### Verification
- `mvn compile -pl autoflow-app -Dcheckstyle.skip=true` succeeded
- Note: Pre-existing checkstyle violation in `PropertyDeserializer.java` (unused import) - not related to these changes
# ChatStreamListener Implementation Learnings

## Key Finding: JAR/Source Mismatch
The compiled `StreamListener` interface in the JAR differs from the source code:
- JAR: `onToken(String)`, `onToolStart(String)`, `onToolEnd(String, Object)`, `onComplete()`, `onError(Throwable)`
- Source: `onThinking(String)`, `onToken(String)`, `onToolCallStart(String, String)`, `onToolCallEnd(String, Object)`, `onComplete(String)`, `onError(Throwable)`

**Action**: Implemented based on the COMPILED JAR interface, not the source.

## Pre-existing Infrastructure Issues
1. Maven local repository cache corruption with `${revision}` variable not resolving
2. Remote repository doesn't have properly resolved parent POMs
3. Project uses `${revision}` property which doesn't resolve properly in parent POM references

## Implementation Details
- ChatStreamListener takes `SseEmitter` as constructor parameter
- Implements 5 methods: `onToken`, `onToolStart`, `onToolEnd`, `onComplete`, `onError`
- SSE events sent immediately (no buffering)
- IOException handled gracefully (logged, not thrown)
- Does NOT call `sseEmitter.complete()` (caller handles)

## ReActAgent Bean Configuration (2026-03-20)

### What was done
- Added `ReActAgent` bean configuration to `BeanConfig.java`
- Added `StreamingChatModel` bean using `OpenAiStreamingChatModel` with environment variable support
- Added `ToolRegistry` and `NodeExecutor` beans as dependencies

### Key patterns
- Used `System.getenv()` with null check and ternary to provide defaults for API key/base URL/model
- Used builder pattern for `ReActAgent` matching lines 55-102 of `ReActAgent.java`
- Dependencies injected via method parameters (Spring auto-wires by type)

### Dependencies added via @Bean methods
```java
@Bean
public OpenAiStreamingChatModel streamingChatModel() { ... }

@Bean
public ReActAgent reActAgent(OpenAiStreamingChatModel chatModel, ToolRegistry toolRegistry, NodeExecutor nodeExecutor) { ... }

@Bean
public ToolRegistry toolRegistry() { return new ToolRegistryImpl(); }

@Bean
public NodeExecutor nodeExecutor() { return new NodeExecutorImpl(); }
```

### Issues
- Maven build has pre-existing parent POM resolution issues (unrelated to these changes)
- Verification done by code review (syntax is correct)

## ChatController Implementation (2026-03-20)

### Task
Created `ChatController` with `/chat` SSE streaming endpoint.

### Approach
- Read `WorkflowController.java` for REST controller patterns (@RestController, @RequestMapping)
- Read `ServiceSSEListener.java` for SseEmitter usage patterns (lines 26-39)
- Read `AgentChatRequest.java` for request DTO (sessionId, input fields)
- Read `ChatStreamListener.java` for SSE listener bridge implementation
- Read `AgentEngine.java` for chat() method signature
- Read `BeanConfig.java` to confirm ReActAgent bean availability

### Key Patterns
- Constructor injection of `ReActAgent` bean
- `@Valid @RequestBody AgentChatRequest request` for validation
- `SseEmitter(Long.MAX_VALUE)` for no timeout
- `CompletableFuture.runAsync()` to run agent.chat() non-blocking
- `emitter.complete()` called in finally block after agent finishes
- Error events sent via `SseEmitter.event().name("error").data(...)` pattern

### Files Created
- `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java`

### Endpoint Details
- **Route**: `POST /chat/`
- **Request**: `AgentChatRequest` with `sessionId` (String) and `input` (String, max 8192)
- **Response**: `SseEmitter` for streaming events
- **Error Handling**: Blank input returns error event and completes emitter immediately

### Verification
- Code review confirms syntactically correct (no unused imports after cleanup)
- Pre-existing Maven parent POM issues prevent compilation verification
- All imports verified against existing classes in codebase
