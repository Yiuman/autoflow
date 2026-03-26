# Fix: History Messages Not Loading

## TL;DR

> **Quick Summary**: Fix bug where clicking chat history records doesn't load or display historical messages.
>
> **Deliverables**:
> - `loadSessionMessages()` action in chat store that fetches and parses historical messages
> - Modified `setActiveSession()` to auto-load messages when switching to a session without loaded messages
>
> **Estimated Effort**: Small (1-2 files, focused change)
> **Parallel Execution**: NO - sequential tasks
> **Critical Path**: Task 1 → Task 2 → QA

---

## Context

### Bug Description
When user clicks on a chat history record in `ChatSidebar`, the historical conversation is not rendered. The chat area shows "Send a message to start chatting" instead of loading the conversation history.

### Root Cause Analysis
1. `loadSessions()` in `chat.ts` only loads session metadata (id, title, modelId) and sets `messages: []` for each session
2. `setActiveSession()` only sets `activeSessionId` - no message loading occurs
3. `getChatMessages(sessionId)` API function exists in `api/chat.ts` (line 32-35) but is **never called**
4. `activeMessages` getter returns empty because `messagesBySession[sessionId]` is never populated

### Files Involved
| File | Role |
|------|------|
| `autoflow-fe/src/stores/chat.ts` | Store with session/message state management |
| `autoflow-fe/src/api/chat.ts` | API with `getChatMessages()` (unused) |
| `autoflow-fe/src/components/Chat/layout/ChatSidebar.vue` | Click handler that calls `setActiveSession()` |

---

## Work Objectives

### Core Objective
When user clicks on a chat history session, automatically fetch and render that session's message history.

### Definition of Done
- [ ] Clicking history session triggers API call to `/api/chat/messages?sessionId=X`
- [ ] Historical messages appear in the chat area immediately after clicking
- [ ] No breaking changes to new conversation flow

### Must Have
- `loadSessionMessages(sessionId)` action that calls `getChatMessages()` and populates the store
- `setActiveSession()` modified to auto-load messages when switching to an unloaded session

### Must NOT Have
- Don't modify API file structure or other unrelated functionality
- Don't break existing new chat flow (creating sessions, sending messages via SSE)

---

## Verification Strategy

### Test Strategy
- **Infrastructure exists**: Vitest (frontend tests exist in project)
- **Automated tests**: Tests-after (add tests for new functionality)
- **No TDD required**: This is a bug fix with existing test infrastructure

### QA Policy
Every task includes agent-executed QA scenarios. Evidence saved to `.sisyphus/evidence/`.

---

## TODOs

- [ ] 1. Add `loadSessionMessages` action to chat store

  **What to do**:
  - Read `api/chat.ts` to understand `getChatMessages()` response format
  - Add `loadSessionMessages(sessionId: string)` action to `stores/chat.ts`
  - Parse API response into `Message` and `MessageBlock` entities
  - Populate `messagesBySession[sessionId]` with message IDs
  - Populate `messageEntities` with parsed messages
  - Handle errors gracefully (console.error, don't crash)

  **Must NOT do**:
  - Don't modify `api/chat.ts` file structure
  - Don't add message loading to `loadSessions()` (that would load ALL histories on startup - inefficient)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple store action addition, clear requirements
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 2

  **References**:
  - `autoflow-fe/src/api/chat.ts:32-35` - `getChatMessages()` API function signature
  - `autoflow-fe/src/stores/chat.ts:100-114` - `loadSessions()` pattern for API calling
  - `autoflow-fe/src/stores/chat.ts:150-163` - `addMessage()` pattern for populating messageEntities
  - `autoflow-fe/src/types/chat.ts:96-112` - `Message` interface structure
  - `autoflow-fe/src/types/chat.ts:27-94` - `MessageBlock` types (MainTextBlock, ThinkingBlock, etc.)

  **Acceptance Criteria**:
  - [ ] `loadSessionMessages` action exists in store
  - [ ] Action calls `getChatMessages(sessionId)` API
  - [ ] API response is parsed into Message entities with blocks
  - [ ] `messagesBySession[sessionId]` is populated with message IDs
  - [ ] `messageEntities` is populated with parsed messages
  - [ ] Error handling: failed API calls logged but don't crash

  **QA Scenarios**:

  ```
  Scenario: Load messages for a session that has history
    Tool: Bash
    Preconditions: Backend returns messages for sessionId "test-session-123"
    Steps:
      1. Import store: `cd autoflow-fe && npx vitest run --grep "loadSessionMessages" --dry-run` (verify test exists)
      2. Or manually: call chatStore.loadSessionMessages("test-session-123") in browser console
      3. Check store state: chatStore.messagesBySession["test-session-123"] should have message IDs
      4. Check store state: chatStore.messageEntities should contain parsed messages
    Expected Result: messagesBySession["test-session-123"] contains array of message IDs (non-empty)
    Evidence: .sisyphus/evidence/task-1-load-messages-success.txt

  Scenario: Load messages for a session with no history
    Tool: Bash  
    Preconditions: Backend returns empty array for sessionId "empty-session"
    Steps:
      1. Call chatStore.loadSessionMessages("empty-session")
      2. Check store state: messagesBySession["empty-session"] should be empty array []
    Expected Result: No crash, messagesBySession populated with empty array
    Evidence: .sisyphus/evidence/task-1-load-empty-success.txt
  ```

  **Commit**: YES
  - Message: `fix(chat): add loadSessionMessages action to load historical messages`
  - Files: `autoflow-fe/src/stores/chat.ts`

---

- [ ] 2. Modify `setActiveSession` to auto-load messages when switching sessions

  **What to do**:
  - Modify `setActiveSession(sessionId: string)` in `stores/chat.ts`
  - After setting `this.activeSessionId = sessionId`
  - Check if `this.messagesBySession[sessionId]` is undefined or empty
  - If empty, call `this.loadSessionMessages(sessionId)`
  - Keep it async-friendly but don't block UI

  **Must NOT do**:
  - Don't make it strictly async (UI should update immediately, messages load in background)
  - Don't call `loadSessionMessages` for NEW session (new sessions have no history to load)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple modification to existing function
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocked By**: Task 1
  - **Blocks**: QA

  **References**:
  - `autoflow-fe/src/stores/chat.ts:116-118` - Current `setActiveSession` implementation
  - `autoflow-fe/src/stores/chat.ts:79-98` - `createSession` pattern (session created with empty messages is normal)
  - Task 1 implementation above

  **Acceptance Criteria**:
  - [ ] `setActiveSession` checks if session has loaded messages before switching
  - [ ] If messages not loaded, triggers `loadSessionMessages` automatically
  - [ ] UI updates immediately (activeSessionId set) even if message loading is in progress
  - [ ] New sessions (created via `createSession`) don't trigger unnecessary API calls

  **QA Scenarios**:

  ```
  Scenario: Click on a history session that has messages in backend
    Tool: Playwright (if E2E) or manual verification
    Preconditions: Session "hist-1" exists in backend with 3 messages
    Steps:
      1. User is on ChatBox page with session list visible
      2. Click on session "hist-1" in ChatSidebar
      3. Observe: activeSessionId should update immediately
      4. Observe: After loading, messages should appear in chat area
    Expected Result: Historical messages visible in chat area within 1-2 seconds
    Evidence: .sisyphus/evidence/task-2-click-history-session.webp

  Scenario: Click between multiple history sessions
    Tool: Playwright or manual
    Preconditions: Multiple sessions with different messages
    Steps:
      1. Click session "A" - wait for messages to load
      2. Click session "B" - messages from B should load
      3. Click back to session "A" - messages should reload (no caching)
    Expected Result: Correct messages shown for each session
    Evidence: .sisyphus/evidence/task-2-switch-sessions.webp
  ```

  **Commit**: YES
  - Message: `fix(chat): auto-load messages when switching to unloaded session`
  - Files: `autoflow-fe/src/stores/chat.ts`

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — Verify all acceptance criteria met, no missing functionality

- [ ] F2. **Code Quality Review** — `npm run type-check` passes, no new lint errors

- [ ] F3. **Real Manual QA** — Click through chat history, verify messages load correctly

- [ ] F4. **Scope Fidelity Check** — Only modified chat store, no unrelated changes

---

## Commit Strategy

- **Final**: `fix(chat): load historical messages when clicking chat history`

---

## Success Criteria

### Verification Commands
```bash
cd autoflow-fe && npm run type-check  # Should pass with no new errors
```

### Final Checklist
- [ ] Clicking history session triggers message loading
- [ ] Historical messages render in chat area
- [ ] No breaking changes to new conversation flow
- [ ] TypeScript compiles without errors
