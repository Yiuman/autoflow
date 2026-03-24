# SSE Chat Database Persistence Implementation Plan

## TL;DR

> **Quick Summary**: Integrate database persistence into the existing SSE chat flow, enabling chat history storage and retrieval. User messages, assistant responses (with thinking content), and auto-generated session titles will be persisted to PostgreSQL via MyBatis-Flex.
>
> **Deliverables**:
> - Backend: `PersistingChatStreamListener` + service methods for progressive upsert + title generation
> - Frontend: Load chat history from backend when selecting sessions
> - End-to-end: Chat messages persist across page refreshes
>
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 → Task 2 → Task 3 → Task 5 → Task 7

---

## Context

### Original Request
用户已有对接前后端的 SSE 大模型对话功能，但未实现数据库存储和查询。需要完美实现数据库存储和查询功能。

### Interview Summary
**Key Discussions**:
- Store thinking_content: Yes
- Frontend query architecture: Pure backend API (not local-first)
- Session title auto-generation: LLM-generated from first user message
- Streaming save strategy: Progressive upsert (save while streaming, not just at the end)
- Full-text search: No

**Research Findings**:
- Backend already has `ChatSession` and `ChatMessage` entities with MyBatis-Flex annotations
- Backend already has `ChatSessionService` and `ChatMessageService` (extends ola-crud BaseService)
- `ChatMessageService` already has `findBySessionId(sessionId)` sorted by create_time
- Frontend already has `getChatSessions()` and `getChatMessages()` API calls (not wired into chat flow)
- Frontend `useChatStore` manages local state only - no DB-backed message loading during chat

### Metis Review
**Identified Gaps** (addressed):
- Progressive upsert needed: Create message → upsert content/thinking → mark complete
- Title generation: Need LLM call before/alongside first message
- Frontend session switching: `setActiveSession()` must call `getChatMessages()` to load history
- Race condition mitigation: One sessionId per SSE connection (enforced by frontend)

---

## Work Objectives

### Core Objective
让 SSE 聊天支持数据库持久化和查询：用户发送消息 → 保存到数据库 → 流式响应 → 增量更新消息 → 前端刷新页面可加载历史记录。

### Concrete Deliverables
- `PersistingChatStreamListener.java` - 同时发送 SSE 和保存到数据库
- `ChatMessageService.upsertMessage()` - 增量更新消息内容
- `ChatSessionService.generateTitle()` - LLM 生成会话标题
- `ChatController` 修改 - 集成持久化监听器
- 前端 `loadMessages()` - 切换会话时从后端加载历史
- 端到端测试 - 发送消息 → 刷新页面 → 聊天记录仍存在

### Definition of Done
- [ ] 发送一条消息 → 数据库 `af_chat_message` 有 2 条记录 (user + assistant)
- [ ] assistant 消息的 `thinking_content` 字段有值
- [ ] 新会话第一条消息后 → `af_chat_session`.`title` 被 LLM 自动生成
- [ ] 刷新页面 → 调用 `GET /chat/sessions` 返回会话列表
- [ ] 点击会话 → 调用 `GET /chat/messages?sessionId=xxx` 返回历史消息

### Must Have
- 用户消息在流式开始前保存到数据库
- Assistant 消息在流式过程中增量更新 (content + thinking_content)
- 流式完成后消息标记为已完成
- 新会话第一条消息后自动生成标题

### Must NOT Have (Guardrails)
- 不修改现有的 `ChatStreamListener`（保持向后兼容其他 SSE 场景）
- 不实现消息全文搜索（用户已确认不需要）
- 不修改 `af_chat_session` 和 `af_chat_message` 表结构（schema 已定义）
- 前端不引入 localStorage 持久化（纯后端 API 架构）

---

## Verification Strategy (MANDATORY)

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: YES (existing backend with JUnit + Spring Boot Test)
- **Automated tests**: Tests-after (add unit tests for new service methods)
- **Framework**: JUnit 5 + Mockito (existing backend test stack)

### QA Policy
Every task includes agent-executed QA scenarios. Evidence saved to `.sisyphus/evidence/`.

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — backend service extensions, MAX PARALLEL):
├── Task 1: Add upsertMessage() to ChatMessageService
├── Task 2: Add generateTitle() to ChatSessionService
├── Task 3: Create PersistingChatStreamListener
└── Task 4: Modify ChatController to integrate persistence

Wave 2 (After Wave 1 — frontend integration):
├── Task 5: Add loadMessages() to useChatStore
├── Task 6: Modify setActiveSession() to load history
├── Task 7: Ensure createSession() persists to backend
└── Task 8: End-to-end QA test

Wave FINAL (After ALL tasks — review):
└── Task F1: Plan compliance audit (oracle)
```

### Dependency Matrix

| Task | Depends On | Blocks |
|------|-----------|---------|
| 1 (upsertMessage) | — | 3, 4 |
| 2 (generateTitle) | — | 3, 4 |
| 3 (PersistingListener) | 1, 2 | 4 |
| 4 (ChatController) | 3 | — |
| 5 (loadMessages) | — | 6, 7 |
| 6 (setActiveSession) | 5 | 8 |
| 7 (createSession) | 5 | 8 |
| 8 (E2E QA) | 6, 7 | — |

### Agent Dispatch Summary

- **1**: **3** — T1 → `quick`, T2 → `quick`, T3 → `unspecified-high`
- **2**: **2** — T4 → `unspecified-high`, T5 → `quick`
- **3**: **2** — T6 → `quick`, T7 → `quick`
- **4**: **1** — T8 → `unspecified-high`
- **FINAL**: **1** — F1 → `oracle`

---

## TODOs

- [ ] 1. Add upsertMessage() to ChatMessageService

  **What to do**:
  - Add `upsertMessage(String id, String sessionId, String role, String content, String thinkingContent, String status)` method to `ChatMessageService`
  - Implementation in `ChatMessageServiceImpl`:
    - If message with `id` exists: UPDATE content, thinkingContent, status fields (append to content for streaming)
    - If message with `id` does not exist: INSERT new record with given fields
  - Use MyBatis-Flex's `update` with `QueryWrapper` for conditional update, or use `saveOrUpdate` pattern
  - Status field values: 'streaming', 'completed', 'error'

  **Test cases to cover**:
  - Upsert creates new message when id not found
  - Upsert updates existing message when id found
  - Content appends during streaming (not overwrites)

  **Must NOT do**:
  - Do not change the existing `findBySessionId()` method
  - Do not modify the table schema

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - `systematic-debugging`: Not needed for simple service method

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Task 3 (PersistingChatStreamListener uses upsertMessage)
  - **Blocked By**: None

  **References**:

  **Pattern References** (existing code to follow):
  - `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatMessageServiceImpl.java:14-18` - Existing `findBySessionId` shows MyBatis-Flex QueryWrapper usage

  **API/Type References** (contracts to implement against):
  - `autoflow-app/src/main/java/io/autoflow/app/model/ChatMessage.java` - Entity with fields: id, sessionId, role, content, thinkingContent, metadata

  **Test References** (testing patterns to follow):
  - None found - add unit tests for new method

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):

  ```
  Scenario: Upsert creates new message when id not found
    Tool: Bash (backend test via JUnit)
    Preconditions: No message with id='new-msg-123'
    Steps:
      1. Call upsertMessage(id='new-msg-123', sessionId='s1', role='user', content='Hello', thinkingContent=null, status='completed')
      2. Query database: SELECT * FROM af_chat_message WHERE id='new-msg-123'
    Expected Result: Message exists with content='Hello', role='user', status='completed'
    Failure Indicators: Message not found, or status is null
    Evidence: .sisyphus/evidence/task-1-upsert-create.txt

  Scenario: Upsert updates existing message appending content
    Tool: Bash (backend test via JUnit)
    Preconditions: Message exists with id='existing-msg', content='Hello', status='streaming'
    Steps:
      1. Call upsertMessage(id='existing-msg', sessionId='s1', role='assistant', content='Hello World', thinkingContent='Thinking more', status='streaming')
      2. Query database: SELECT content, thinking_content, status FROM af_chat_message WHERE id='existing-msg'
    Expected Result: content='Hello World', thinking_content='Thinking more', status='streaming'
    Evidence: .sisyphus/evidence/task-1-upsert-update.txt
  ```

  **Evidence to Capture**:
  - [ ] Evidence files for both scenarios

  **Commit**: YES
  - Message: `feat(chat): add upsertMessage method to ChatMessageService`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/service/ChatMessageService.java`, `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatMessageServiceImpl.java`
  - Pre-commit: N/A

- [ ] 2. Add generateTitle() to ChatSessionService

  **What to do**:
  - Add `generateTitle(String sessionId, String firstUserMessage)` to `ChatSessionService`
  - Implementation: Use `LlmService` or `ModelRegistry` to call LLM with a simple prompt:
    ```
    Given this user message, generate a short title (max 30 Chinese characters) for this chat session. Only return the title, nothing else.
    Message: "{firstUserMessage}"
    Title:
    ```
  - Use the session's `modelId` (or default model) for the LLM call
  - Save the generated title to the session via `ChatSessionService.update()`
  - If LLM call fails or returns empty, use fallback: first 20 characters of message + "..." if longer

  **Test cases to cover**:
  - Title generated from first user message (LLM call succeeds)
  - Title truncated to 30 chars
  - Fallback used when LLM call fails

  **Must NOT do**:
  - Do not modify the `af_chat_session` table schema
  - Do not block the main chat flow if title generation is slow (make it non-blocking or fast-fail)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - `systematic-debugging`: Not needed for straightforward service method

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Task 3 (PersistingChatStreamListener may need title generation)
  - **Blocked By**: None

  **References**:

  **Pattern References** (existing code to follow):
  - `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java:94` - Shows how `ModelRegistry.getModel(modelId)` is used to get a streaming model
  - `autoflow-plugins/autoflow-llm/src/main/java/io/autoflow/plugin/llm/LlmService.java` - Non-streaming LLM call pattern

  **API/Type References** (contracts to implement against):
  - `autoflow-app/src/main/java/io/autoflow/app/model/ChatSession.java` - Entity with title field
  - `autoflow-app/src/main/java/io/autoflow/app/config/ModelRegistry.java` - How to get ChatModel

  **Test References** (testing patterns to follow):
  - None found

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):

  ```
  Scenario: Generate title from user message
    Tool: Bash (backend test)
    Preconditions: Session exists with id='session-1', title=null
    Steps:
      1. Call generateTitle(sessionId='session-1', firstUserMessage='帮我分析销售数据')
      2. Query database: SELECT title FROM af_chat_session WHERE id='session-1'
    Expected Result: title is not null, length <= 30, contains meaningful summary of the message
    Failure Indicators: title still null, title is empty, title > 30 chars
    Evidence: .sisyphus/evidence/task-2-generate-title.txt
  ```

  **Evidence to Capture**:
  - [ ] Evidence file for title generation

  **Commit**: YES
  - Message: `feat(chat): add generateTitle method to ChatSessionService`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/service/ChatSessionService.java`, `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatSessionServiceImpl.java`
  - Pre-commit: N/A

- [ ] 3. Create PersistingChatStreamListener

  **What to do**:
  - Create new class `PersistingChatStreamListener` that implements `StreamListener`
  - This class wraps the existing `ChatStreamListener` AND adds DB persistence
  - Constructor: `PersistingChatStreamListener(SseEmitter emitter, ChatMessageService chatMessageService, ChatSessionService chatSessionService, String sessionId, String modelId)`
  - Fields to track: `userMessageId`, `assistantMessageId`, `sessionId`, accumulated content, accumulated thinking
  - **onThinking(thinking)**: Accumulate thinkingContent, call `chatMessageService.upsertMessage(assistantMessageId, ...)` to update DB
  - **onToken(token)**: Append to content, call `chatMessageService.upsertMessage(assistantMessageId, ...)` to update DB
  - **onToolCallStart/End**: Forward to SSE emitter (ChatStreamListener) but do NOT persist (not in scope)
  - **onComplete(fullOutput)**: Final upsert with status='completed', forward to SSE
  - **onError(e)**: Upsert with status='error', forward to SSE
  - Also forward ALL events to the underlying SSE emitter (delegate pattern)

  **Flow**:
  1. Before calling `reActAgent.chat()`: Create user message record (role='user') and assistant message record (role='assistant', status='streaming')
  2. During streaming: Each `onToken`/`onThinking` → upsert message + send SSE
  3. On complete: Final upsert with status='completed' + send SSE 'complete'
  4. On error: Upsert with status='error' + send SSE 'error'

  **Test cases to cover**:
  - onToken calls upsertMessage with appended content
  - onThinking calls upsertMessage with updated thinkingContent
  - onComplete calls upsertMessage with status='completed'
  - onError calls upsertMessage with status='error'

  **Must NOT do**:
  - Do not modify `ChatStreamListener.java` (keep it unchanged for other SSE use cases)
  - Do not handle tool_call persistence (not in scope)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: []
  - **Why**: Needs to understand SSE + streaming + DB interaction patterns

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Task 4 (ChatController uses this)
  - **Blocked By**: Tasks 1, 2 (needs upsertMessage and generateTitle)

  **References**:

  **Pattern References** (existing code to follow):
  - `autoflow-app/src/main/java/io/autoflow/app/listener/ChatStreamListener.java` - Existing StreamListener implementation to understand event types
  - `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java:90-91` - How SseEmitter and ChatStreamListener are created

  **API/Type References** (contracts to implement against):
  - `autoflow-agent/src/main/java/io/autoflow/agent/StreamListener.java` - Interface with onThinking, onToken, onToolCallStart, onToolCallEnd, onComplete, onError
  - `autoflow-app/src/main/java/io/autoflow/app/model/sse/AgentSSEEvent.java` - SSE event model

  **Test References** (testing patterns to follow):
  - None found

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):

  ```
  Scenario: onToken accumulates content and upserts
    Tool: Unit test (JUnit + Mockito)
    Preconditions: PersistingChatStreamListener created with mock services, assistantMessageId='asst-1'
    Steps:
      1. Call listener.onToken("Hello")
      2. Call listener.onToken(" World")
      3. Verify chatMessageService.upsertMessage was called twice with accumulated content
    Expected Result: First call has content='Hello', second call has content='Hello World'
    Failure Indicators: Content not accumulated, upsert not called
    Evidence: .sisyphus/evidence/task-3-token-accumulate.txt

  Scenario: onComplete marks message as completed
    Tool: Unit test (JUnit + Mockito)
    Preconditions: PersistingChatStreamListener created with mock services
    Steps:
      1. Call listener.onComplete("Final response")
      2. Verify upsertMessage called with status='completed'
    Expected Result: upsertMessage called with final content and status='completed'
    Evidence: .sisyphus/evidence/task-3-complete.txt
  ```

  **Evidence to Capture**:
  - [ ] Evidence files for both scenarios

  **Commit**: YES
  - Message: `feat(chat): create PersistingChatStreamListener for DB persistence`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/listener/PersistingChatStreamListener.java`
  - Pre-commit: N/A

---

## Final Verification Wave

- [ ] 4. Modify ChatController to integrate persistence

  **What to do**:
  - Modify `ChatController.chat()` to use `PersistingChatStreamListener` instead of `ChatStreamListener`
  - Inject `ChatMessageService` and `ChatSessionService` into `ChatController`
  - **Before calling `reActAgent.chat()`**:
    1. Save user message to DB: `chatMessageService.save(userMessage)` with role='user'
    2. Create assistant message placeholder: `chatMessageService.save(assistantMessage)` with role='assistant', status='streaming'
    3. If this is first message in session (check session message count), call `chatSessionService.generateTitle(sessionId, userInput)` asynchronously
  - Pass the userMessageId and assistantMessageId to `PersistingChatStreamListener`
  - **Modified method signature**: `PersistingChatStreamListener(listener, chatMessageService, sessionId, userMessageId, assistantMessageId, modelId)`
  - Keep existing error handling and SSE emitter setup unchanged

  **Test cases to cover**:
  - User message saved before streaming starts
  - Assistant message placeholder created before streaming
  - Title generated for first message in new session

  **Must NOT do**:
  - Do not remove the SSE streaming functionality (must still send events to frontend)
  - Do not change the `/chat` endpoint signature (keep backward compatibility)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: []
  - **Why**: Controller modification with multiple service dependencies

  **Parallelization**:
  - **Can Run In Parallel**: NO (depends on Task 3)
  - **Parallel Group**: Wave 2
  - **Blocks**: None (is the last backend task)
  - **Blocked By**: Task 3

  **References**:

  **Pattern References** (existing code to follow):
  - `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java:90-105` - Existing SSE emitter creation and async chat flow

  **API/Type References** (contracts to implement against):
  - `autoflow-app/src/main/java/io/autoflow/app/listener/PersistingChatStreamListener.java` - New listener to use (Task 3)

  **Test References** (testing patterns to follow):
  - None found

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):

  ```
  Scenario: Chat saves user message before streaming
    Tool: Bash (curl to /chat endpoint with SSE)
    Preconditions: Session exists with id='session-1' (created via POST /chat/session)
    Steps:
      1. POST /chat with sessionId='session-1', input='Hello AI'
      2. While streaming, query DB: SELECT * FROM af_chat_message WHERE session_id='session-1' AND role='user'
    Expected Result: User message exists in DB before SSE 'complete' event
    Evidence: .sisyphus/evidence/task-4-user-message-saved.txt

  Scenario: Assistant message created with streaming status
    Tool: Bash (curl to /chat endpoint with SSE)
    Preconditions: Session exists
    Steps:
      1. POST /chat with sessionId='session-1', input='Hello'
      2. Check DB immediately after first token event
    Expected Result: Assistant message record exists with status='streaming'
    Evidence: .sisyphus/evidence/task-4-assistant-message-streaming.txt
  ```

  **Evidence to Capture**:
  - [ ] Evidence files for both scenarios

  **Commit**: YES
  - Message: `feat(chat): integrate persistence into ChatController`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java`
  - Pre-commit: N/A

- [ ] 5. Add loadMessages() to useChatStore

  **What to do**:
  - In `autoflow-fe/src/stores/chat.ts`, add `async loadMessages(sessionId: string)` action
  - Call `getChatMessages(sessionId)` from `api/chat.ts`
  - Transform backend `ChatMessage` records into local `Message` entities:
    - Map `content` → message content
    - Map `role` → 'user' or 'assistant'
    - Map `thinkingContent` → create a `ThinkingBlock` if not empty
    - Map `createTime` → `createdAt`
  - Store transformed messages in `messageEntities` and update `messagesBySession[sessionId]`
  - Handle loading state: set `isLoading = true` during fetch, `false` after

  **Test cases to cover**:
  - Load messages for a session with existing messages
  - Handle empty session (no messages)
  - Transform thinkingContent into ThinkingBlock

  **Must NOT do**:
  - Do not make API calls for every message (only on session switch)
  - Do not lose existing local messages when loading from DB

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Why**: Simple store action addition

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 6, 7)
  - **Blocks**: Task 6 (setActiveSession uses loadMessages)
  - **Blocked By**: None

  **References**:

  **Pattern References** (existing code to follow):
  - `autoflow-fe/src/stores/chat.ts:97-111` - Existing `loadSessions()` action pattern showing how API data is transformed
  - `autoflow-fe/src/api/chat.ts:28-31` - Existing `getChatMessages()` API call

  **API/Type References** (contracts to implement against):
  - `autoflow-fe/src/stores/chat.ts` - Store structure: `messageEntities`, `messagesBySession`
  - `autoflow-fe/src/types/chat.ts:96-111` - `Message` interface, `ThinkingBlock` type

  **Test References** (testing patterns to follow):
  - None found (Vue store testing not common in this codebase)

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):

  ```
  Scenario: Load messages populates store correctly
    Tool: Playwright (frontend)
    Preconditions: Session 's1' has 2 messages in DB (user + assistant)
    Steps:
      1. Call useChatStore().loadMessages('s1')
      2. Check store state: messageEntities has 2 messages
    Expected Result: store.messagesBySession['s1'] has 2 message IDs, messageEntities populated
    Failure Indicators: store is empty, wrong number of messages
    Evidence: .sisyphus/evidence/task-5-load-messages.txt
  ```

  **Evidence to Capture**:
  - [ ] Evidence file for loadMessages

  **Commit**: YES
  - Message: `feat(chat): add loadMessages action to chat store`
  - Files: `autoflow-fe/src/stores/chat.ts`
  - Pre-commit: N/A

- [ ] 6. Modify setActiveSession() to load history

  **What to do**:
  - Modify `setActiveSession(sessionId: string)` in `useChatStore`
  - When switching to a different session, call `loadMessages(sessionId)` to fetch history from DB
  - Only load if `messagesBySession[sessionId]` is empty (avoid re-loading on rapid switches)
  - Keep existing logic: update `activeSessionId`

  **Test cases to cover**:
  - Switching to a session loads its messages
  - Switching to already-loaded session does not re-fetch

  **Must NOT do**:
  - Do not always re-fetch (performance)
  - Do not change the existing `setActiveSession` behavior for non-session-id changes

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 5, 7)
  - **Blocks**: Task 8 (E2E test needs session switching)
  - **Blocked By**: Task 5

  **References**:

  **Pattern References** (existing code to follow):
  - `autoflow-fe/src/stores/chat.ts:113-115` - Existing `setActiveSession` implementation

  **Test References** (testing patterns to follow):
  - None

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):

  ```
  Scenario: setActiveSession loads messages for new session
    Tool: Playwright (frontend)
    Preconditions: Session 's1' exists but not loaded in store
    Steps:
      1. Call useChatStore().setActiveSession('s1')
      2. Check that loadMessages was called with 's1'
    Expected Result: loadMessages('s1') was called
    Evidence: .sisyphus/evidence/task-6-set-active-loads.txt

  Scenario: setActiveSession skips load if already cached
    Tool: Playwright (frontend)
    Preconditions: Session 's1' already loaded in store
    Steps:
      1. Call useChatStore().setActiveSession('s1') (already active)
      2. Verify no additional API call made
    Expected Result: No re-fetch of messages
    Evidence: .sisyphus/evidence/task-6-set-active-cached.txt
  ```

  **Evidence to Capture**:
  - [ ] Evidence files for both scenarios

  **Commit**: YES
  - Message: `feat(chat): load messages on session switch`
  - Files: `autoflow-fe/src/stores/chat.ts`
  - Pre-commit: N/A

- [ ] 7. Ensure createSession() persists to backend

  **What to do**:
  - Current `createSession()` in store calls `createChatSession(modelId)` from API
  - The API call `POST /chat/session` creates a session but does NOT save to DB (this was already confirmed - it just returns a UUID)
  - Modify the backend `POST /chat/session` endpoint in `ChatController` to also save the session to DB using `ChatSessionService.save()`
  - Alternatively: If session creation should happen at first chat message (not at session creation), then ensure the session is saved when first chat message is sent
  - **Key insight**: Looking at `ChatController.createSession()` (line 39-44), it just generates a UUID and returns it. The session is NOT persisted. Fix this by saving to DB in `ChatController.createSession()`.

  **Test cases to cover**:
  - createSession API call now saves session to DB
  - Session appears in DB immediately after creation

  **Must NOT do**:
  - Do not break the existing API contract (still returns sessionId)

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 5, 6)
  - **Blocks**: Task 8 (E2E test)
  - **Blocked By**: None (service already exists)

  **References**:

  **Pattern References** (existing code to follow):
  - `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java:39-44` - Existing createSession method
  - `autoflow-app/src/main/java/io/autoflow/app/service/ChatSessionService.java` - CRUD service to use

  **Test References** (testing patterns to follow):
  - None

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):

  ```
  Scenario: createSession saves to database
    Tool: Bash (curl to POST /chat/session)
    Preconditions: Empty database
    Steps:
      1. POST /chat/session with modelId='gpt-4'
      2. Query DB: SELECT * FROM af_chat_session
    Expected Result: Session record exists with modelId='gpt-4', status='ACTIVE'
    Evidence: .sisyphus/evidence/task-7-create-session-persists.txt
  ```

  **Evidence to Capture**:
  - [ ] Evidence file for createSession persistence

  **Commit**: YES
  - Message: `feat(chat): persist session to database on creation`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java`
  - Pre-commit: N/A

- [ ] 8. End-to-end QA test

  **What to do**:
  - Run full chat flow and verify persistence:
    1. Create session via `POST /chat/session`
    2. Send chat via `POST /chat` with SSE
    3. Verify user message in DB
    4. Verify assistant message in DB with thinking_content
    5. Verify session title was generated
    6. Refresh frontend or call `GET /chat/sessions` and `GET /chat/messages`
    7. Verify all data loads correctly
  - Use curl for API testing, or Playwright for full browser flow

  **Test cases to cover**:
  - Full chat → DB verification
  - Page refresh → history loads

  **Must NOT do**:
  - Do not skip any step in the flow

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: [`playwright`]

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 6, 7

  **References**:

  **Pattern References** (existing code to follow):
  - `autoflow-fe/src/api/chat.ts:42-109` - chatSSE usage in frontend

  **Test References** (testing patterns to follow):
  - None

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):

  ```
  Scenario: Complete chat flow persists to DB
    Tool: Bash (curl) + PostgreSQL
    Preconditions: App running, empty DB
    Steps:
      1. POST /chat/session → get sessionId
      2. POST /chat with sessionId + "Hello AI" → SSE stream completes
      3. Query: SELECT role, content, thinking_content FROM af_chat_message WHERE session_id='xxx'
      4. Query: SELECT title FROM af_chat_session WHERE id='xxx'
    Expected Result: 2 messages (user + assistant), assistant has thinking_content, session has title
    Evidence: .sisyphus/evidence/task-8-e2e-chat.txt

  Scenario: Chat history loads after refresh
    Tool: Bash (curl)
    Preconditions: After Scenario 1
    Steps:
      1. GET /chat/sessions → verify session in list
      2. GET /chat/messages?sessionId=xxx → verify 2 messages returned
    Expected Result: All data returns correctly
    Evidence: .sisyphus/evidence/task-8-e2e-history-loads.txt
  ```

  **Evidence to Capture**:
  - [ ] Evidence files for both scenarios

  **Commit**: NO (QA task only)

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — `oracle`

  Read the plan end-to-end. For each "Must Have": verify implementation exists (read file, curl endpoint, run command). For each "Must NOT Have": search codebase for forbidden patterns — reject with file:line if found. Check evidence files exist in .sisyphus/evidence/. Compare deliverables against plan.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

  **Files to check**:
  - `autoflow-app/src/main/java/io/autoflow/app/service/ChatMessageService.java` - upsertMessage method
  - `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatMessageServiceImpl.java` - upsertMessage implementation
  - `autoflow-app/src/main/java/io/autoflow/app/service/ChatSessionService.java` - generateTitle method
  - `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatSessionServiceImpl.java` - generateTitle implementation
  - `autoflow-app/src/main/java/io/autoflow/app/listener/PersistingChatStreamListener.java` - new listener
  - `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java` - integrated persistence
  - `autoflow-fe/src/stores/chat.ts` - loadMessages, setActiveSession modifications
  - `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java` - createSession now persists

---

## Success Criteria

### Verification Commands
```bash
# Backend: Start app, send chat request, verify DB records
psql -d autoflow -c "SELECT id, role, content, thinking_content FROM af_chat_message WHERE session_id = 'xxx';"
psql -d autoflow -c "SELECT id, title FROM af_chat_session WHERE id = 'xxx';"

# Frontend: Refresh page, verify sessions and messages load
curl -s http://localhost:8080/api/chat/sessions | jq '.data.records[].title'
curl -s "http://localhost:8080/api/chat/messages?sessionId=xxx" | jq '.data.records[].content'
```

### Final Checklist
- [ ] All "Must Have" present
- [ ] All "Must NOT Have" absent
- [ ] All unit tests pass
- [ ] E2E chat flow verified
