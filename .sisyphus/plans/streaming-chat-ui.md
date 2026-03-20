# Streaming Chat UI (Claude Style) Implementation Plan

## TL;DR

> **Quick Summary**: Build a Vue 3 streaming chat UI for Agent dialogue with Claude-style card layout, collapsible thinking blocks, and collapsible tool call cards.
> 
> **Deliverables**:
> - `src/components/StreamingChat/` - 5 new Vue components
> - `src/composables/useStreamingChat.ts` - SSE handling composable
> - `src/stores/chat.ts` - Pinia store for chat state
> - `src/types/streaming-chat.d.ts` - Extended TypeScript types
> - Unit tests for composable and components
> 
> **Estimated Effort**: Medium-Large
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Types → Composable → Components

---

## Context

### Original Request
Build a streaming chat UI for Agent dialogue with Claude style, including think (inner monologue) and tool call event handling.

### Interview Summary
**Key Discussions**:
- **Scope**: Agent对话 (AI chat with streaming, tool calls)
- **Style**: Claude风格 - 卡片式, 思考过程collapsible
- **Think Display**: 显示为AI的"内心独白" (collapsible section)
- **Tool Calls**: 折叠式展示 (collapsible cards with status)
- **Backend**: 暂不改动后端，仅参考StreamListener事件枚举
- **Testing**: TDD approach

### Backend Event Reference (StreamListener.java)
```java
onToken(String token)                    // LLM流式输出
onToolStart(String toolName)             // 工具开始执行
onToolEnd(String toolName, Object result) // 工具执行结束
onToolCallComplete(String toolName, String arguments) // 工具调用完成
onComplete()                            // 对话完成
onComplete(String fullOutput)           // 完成并输出完整内容
onError(Throwable e)                    // 错误
```

### Research Findings
**Reference Projects**:
- **shadcn.io/ai** (25+ React AI components) - Reasoning, Tool, Message patterns
- **Nuxt UI Chat** (Vue-native equivalents) - ChatReasoning, ChatTool components
- **LobeChat** (73k stars) - Comprehensive streaming implementation
- **VS Code Copilot Chat** - ChatThinkingContentPart, ChatCollapsibleContentPart
- **screenpipe** - PiEvent event handler pattern

**Key Patterns Discovered**:
1. SSE Client: `@microsoft/fetch-event-source` with proper typing
2. Event Handler: `reducePiEvent` pattern for incremental state updates
3. Thinking UI: Collapsible with shimmer during streaming
4. Tool Cards: Status badge (streaming/running/complete/error) + expandable JSON

---

## Work Objectives

### Core Objective
Create a complete streaming chat UI for Agent dialogue with Claude-style cards, collapsible thinking blocks, and collapsible tool call cards.

### Concrete Deliverables
- [ ] `src/types/streaming-chat.d.ts` - Extended Message/ToolCall types
- [ ] `src/stores/chat.ts` - Pinia store for messages and streaming state
- [ ] `src/composables/useStreamingChat.ts` - SSE logic + state management
- [ ] `src/components/StreamingChat/ThinkingBlock.vue` - Collapsible thinking section
- [ ] `src/components/StreamingChat/ToolCallCard.vue` - Tool execution card
- [ ] `src/components/StreamingChat/ToolCallList.vue` - Container for tool calls
- [ ] `src/components/StreamingChat/StreamingMessage.vue` - Message with tool/think
- [ ] `src/components/StreamingChat/StreamingChat.vue` - Main container
- [ ] `src/components/StreamingChat/StreamingChatView.vue` - Page component with routing
- [ ] Unit tests for composable and components

### Definition of Done
- [ ] Token streaming renders character-by-character
- [ ] Tool cards appear immediately on `onToolStart`
- [ ] Tool results display in collapsed card on `onToolEnd`
- [ ] Thinking block is collapsible and shows during streaming
- [ ] Markdown renders correctly in streamed content
- [ ] Error state displays on `onError`
- [ ] All tests pass (TDD)

### Must Have
1. Incremental token streaming (not wait-for-complete)
2. Tool card appears within 100ms of `onToolStart`
3. Tool result shows in collapsed card on `onToolEnd`
4. Collapsible "thinking" section (default: collapsed for completed, expanded for streaming)
5. Markdown rendering in streamed content
6. Works with existing `@microsoft/fetch-event-source`
7. Mock SSE for testing (no backend dependency)

### Must NOT Have (Guardrails)
1. ~~File uploads~~ - NOT in scope
2. ~~Message branching/threading~~ - Future consideration only
3. ~~Real backend integration~~ - Mock SSE only for now
4. ~~Rich JSON viewer~~ - Collapsible JSON only
5. ~~localStorage persistence~~ - Session only
6. ~~Multiple concurrent streams~~ - Single stream only
7. ~~Authentication/authorization~~ - Assume pre-authed

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES (bun test configured)
- **Automated tests**: TDD (tests-first approach)
- **Framework**: vitest (Vue 3 standard) or bun test

### QA Policy
Every task includes agent-executed QA scenarios - executing agent verifies by running Playwright/tests directly.

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Foundation - types + store + composable):
├── Task 1: Define extended types (StreamingMessage, ToolCall, ChatEvent)
├── Task 2: Create Pinia store (useChatStore)
├── Task 3: Create useStreamingChat composable with mock SSE
└── Task 4: Write unit tests for composable

Wave 2 (Components - UI building blocks):
├── Task 5: ThinkingBlock.vue (collapsible thinking section)
├── Task 6: ToolCallCard.vue (tool execution card)
├── Task 7: ToolCallList.vue (container for tool calls)
├── Task 8: Write unit tests for components
└── Task 9: StreamingMessage.vue (message with tool/think)

Wave 3 (Integration - full container):
├── Task 10: StreamingChat.vue (main container)
├── Task 11: StreamingChatView.vue (page with routing)
├── Task 12: Integration test with mock SSE
└── Task 13: Final verification

Wave FINAL (Verification):
├── Task F1: Plan compliance audit
├── Task F2: Code quality review
├── Task F3: Manual QA with Playwright
└── Task F4: Scope fidelity check
```

### Dependency Matrix

| Task | Blocked By | Blocks |
|------|------------|--------|
| T1: Types | - | T2, T3 |
| T2: Store | T1 | T3 |
| T3: Composable | T1, T2 | T4, T5-T9 |
| T4: Composable tests | T3 | F1-F4 |
| T5: ThinkingBlock | T3 | T9 |
| T6: ToolCallCard | T3 | T7, T9 |
| T7: ToolCallList | T3, T6 | T9 |
| T8: Component tests | T5, T6, T7 | F1-F4 |
| T9: StreamingMessage | T3, T5, T6, T7 | T10 |
| T10: StreamingChat | T9 | T11 |
| T11: StreamingChatView | T10 | T12 |
| T12: Integration test | T11 | F1-F4 |
| F1-F4: Final verification | ALL | User approval |

---

## TODOs

- [ ] 1. **Define extended types** — `src/types/streaming-chat.d.ts`

  **What to do**:
  - Create `StreamingMessage` interface extending existing Message
  - Create `ToolCall` interface with id, name, status, arguments, result
  - Create `ChatEvent` union type for SSE events
  - Create `StreamListener` interface mapping backend events

  **Must NOT do**:
  - Don't modify existing `types/chat.d.ts`

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: Type definition only, straightforward
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with T2, T3)
  - **Blocks**: T2, T3
  - **Blocked By**: None

  **References**:
  - `src/types/chat.d.ts:1-11` - Existing simple Message type to extend
  - `autoflow-agent/src/main/java/io/autoflow/agent/StreamListener.java` - Backend event interface
  - `screenpipe/pi-event-handler.ts` - PiEvent type pattern for ChatEvent

  **Acceptance Criteria**:
  - [ ] `StreamingMessage` has: id, role, content, thinking?, toolCalls?, status, createdAt
  - [ ] `ToolCall` has: id, name, status (pending/running/complete/error), arguments?, result?
  - [ ] `ChatEvent` union type covers: token, toolStart, toolEnd, toolCallComplete, complete, error
  - [ ] TypeScript compiles without errors

  **QA Scenarios**:
  ```
  Scenario: Type definitions are valid TypeScript
    Tool: Bash (tsc)
    Steps:
      1. Run tsc --noEmit on types file
    Expected Result: No compilation errors
    Evidence: .sisyphus/evidence/task-1-types-valid.{ext}

  Scenario: All required fields are present
    Tool: Bash (grep)
    Steps:
      1. grep for "interface StreamingMessage" and verify fields
    Expected Result: Contains id, role, content, status, createdAt
    Evidence: .sisyphus/evidence/task-1-fields.{ext}
  ```

  **Commit**: YES
  - Message: `feat(types): add streaming chat type definitions`
  - Files: `src/types/streaming-chat.d.ts`

---

- [ ] 2. **Create Pinia store** — `src/stores/chat.ts`

  **What to do**:
  - Create `useChatStore` with messages array
  - Add streaming state: currentMessage, isStreaming, error
  - Add actions: addMessage, updateCurrentMessage, appendToken, addToolCall, completeToolCall, setError
  - Add getter: sortedMessages

  **Must NOT do**:
  - Don't add persistence (localStorage) - session only
  - Don't add multiple conversation support

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: Store pattern is straightforward, follows existing useServiceStore
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with T1, T3)
  - **Blocks**: T3
  - **Blocked By**: T1

  **References**:
  - `src/stores/service.ts:1-50` - Existing Pinia store pattern to follow
  - `screenpipe/pi-event-handler.ts:50-100` - reducePiEvent state update pattern

  **Acceptance Criteria**:
  - [ ] Store has messages: StreamingMessage[]
  - [ ] Store has isStreaming: boolean
  - [ ] Store has appendToken(token) action
  - [ ] Store has addToolCall(toolCall) action
  - [ ] Store has completeToolCall(id, result) action
  - [ ] Tests pass: bun test src/stores/chat.test.ts

  **QA Scenarios**:
  ```
  Scenario: appendToken adds to current message
    Tool: Bash (bun test)
    Preconditions: Store has an empty assistant message
    Steps:
      1. Call store.appendToken("Hello")
      2. Call store.appendToken(" World")
    Expected Result: currentMessage.content === "Hello World"
    Evidence: .sisyphus/evidence/task-2-append-token.{ext}

  Scenario: addToolCall creates new tool call
    Tool: Bash (bun test)
    Preconditions: Store has a message
    Steps:
      1. Call store.addToolCall({ id: "1", name: "search", status: "running" })
    Expected Result: message.toolCalls.length === 1
    Evidence: .sisyphus/evidence/task-2-add-tool.{ext}
  ```

  **Commit**: YES
  - Message: `feat(store): add chat Pinia store for streaming state`
  - Files: `src/stores/chat.ts`

---

- [ ] 3. **Create useStreamingChat composable** — `src/composables/useStreamingChat.ts`

  **What to do**:
  - Create composable wrapping SSE connection using `@microsoft/fetch-event-source`
  - Implement event handlers mapping to store actions
  - Add abort controller for cancellation
  - Create mock SSE generator for testing without backend
  - Expose: sendMessage(), abort(), isStreaming, error

  **Must NOT do**:
  - Don't connect to real backend - use mock
  - Don't handle multiple concurrent streams

  **Recommended Agent Profile**:
  > **Category**: `unspecified-high`
  > **Reason**: Core SSE logic, needs careful error handling
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with T1, T2)
  - **Blocks**: T4, T5-T9
  - **Blocked By**: T1, T2

  **References**:
  - `src/views/FlowDesigner/flowsse.ts:1-73` - Existing SSE implementation to follow
  - `Hiram-Wong/zyfun/packages/shared/modules/request/sse/Sse.ts` - SSE pattern with fetchEventSource
  - `screenpipe/pi-event-handler.ts:100-200` - reducePiEvent event handling

  **Acceptance Criteria**:
  - [ ] Uses `@microsoft/fetch-event-source` for SSE
  - [ ] onToken handler calls store.appendToken()
  - [ ] onToolStart handler calls store.addToolCall()
  - [ ] onToolEnd handler calls store.completeToolCall()
  - [ ] onError handler calls store.setError()
  - [ ] onComplete marks streaming complete
  - [ ] Mock mode available for testing
  - [ ] Tests pass: bun test src/composables/useStreamingChat.test.ts

  **QA Scenarios**:
  ```
  Scenario: sendMessage triggers SSE connection
    Tool: Bash (bun test)
    Preconditions: Mock SSE ready
    Steps:
      1. Call composable.sendMessage("Hello")
      2. Wait for first token
    Expected Result: store.isStreaming === true
    Evidence: .sisyphus/evidence/task-3-send-message.{ext}

  Scenario: Mock SSE generates token events
    Tool: Bash (bun test)
    Steps:
      1. Start mock SSE with tokens ["H", "i"]
      2. Call composable.sendMessage("test")
    Expected Result: Two tokens received, content === "Hi"
    Evidence: .sisyphus/evidence/task-3-mock-sse.{ext}

  Scenario: abort stops streaming
    Tool: Bash (bun test)
    Preconditions: Streaming in progress
    Steps:
      1. Call composable.sendMessage("test")
      2. Call composable.abort()
    Expected Result: isStreaming === false within 500ms
    Evidence: .sisyphus/evidence/task-3-abort.{ext}
  ```

  **Commit**: YES
  - Message: `feat(composable): add useStreamingChat with SSE handling`
  - Files: `src/composables/useStreamingChat.ts`

---

- [ ] 4. **Write unit tests for composable** — `src/composables/useStreamingChat.test.ts`

  **What to do**:
  - Test appendToken behavior
  - Test tool call state transitions
  - Test error handling
  - Test abort functionality
  - Test mock SSE generation

  **Must NOT do**:
  - Don't test UI components - that's separate

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: TDD tests, well-defined expected behaviors
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with T1, T2, T3)
  - **Blocks**: F1-F4
  - **Blocked By**: T3

  **References**:
  - `autoflow-fe/src/**/*.test.ts` - Existing test patterns (if any)

  **Acceptance Criteria**:
  - [ ] Test: appendToken updates message content
  - [ ] Test: addToolCall creates tool call in message
  - [ ] Test: completeToolCall updates tool status and result
  - [ ] Test: onError sets error state
  - [ ] Test: abort stops streaming within 500ms
  - [ ] All tests pass

  **QA Scenarios**:
  ```
  Scenario: All tests pass
    Tool: Bash (bun test)
    Steps:
      1. Run bun test src/composables/useStreamingChat.test.ts
    Expected Result: All tests PASS, 0 failures
    Evidence: .sisyphus/evidence/task-4-tests-pass.{ext}
  ```

  **Commit**: YES
  - Message: `test(composable): add unit tests for useStreamingChat`
  - Files: `src/composables/useStreamingChat.test.ts`

---

- [ ] 5. **Create ThinkingBlock.vue** — `src/components/StreamingChat/ThinkingBlock.vue`

  **What to do**:
  - Create collapsible section for AI thinking/reasoning
  - Props: thinking (string), streaming (boolean)
  - Default: collapsed for completed, expanded for streaming
  - Show shimmer effect while streaming
  - Show duration if available
  - Use brain icon

  **Must NOT do**:
  - Don't show if thinking is empty

  **Recommended Agent Profile**:
  > **Category**: `visual-engineering`
  > **Reason**: UI component with styling, animations
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T6, T7, T8)
  - **Blocks**: T9
  - **Blocked By**: T3

  **References**:
  - `shadcn.io/ai/reasoning` - Reasoning component design
  - `microsoft/vscode/src/vs/workbench/contrib/chat/browser/widget/chatContentParts/chatThinkingContentPart.ts` - Thinking implementation
  - `src/components/Chat/Message.vue:1-85` - Existing message styling to match

  **Acceptance Criteria**:
  - [ ] Props: thinking: string, streaming: boolean
  - [ ] Shows brain icon + "Thinking..." while streaming
  - [ ] Collapsible - click to expand/collapse
  - [ ] Default state: collapsed (completed) or expanded (streaming)
  - [ ] Shimmer animation while streaming
  - [ ] Don't render if thinking is empty
  - [ ] Visual match with existing Chat/Message styling

  **QA Scenarios**:
  ```
  Scenario: Shows thinking content when provided
    Tool: Playwright
    Preconditions: Component rendered with thinking="Let me think..."
    Steps:
      1. Verify thinking text is visible
      2. Verify brain icon is shown
    Expected Result: Thinking content displayed
    Evidence: .sisyphus/evidence/task-5-thinking-visible.{ext}

  Scenario: Collapsible behavior
    Tool: Playwright
    Preconditions: Component rendered
    Steps:
      1. Click collapse toggle
      2. Verify content is hidden
      3. Click expand toggle
      4. Verify content is visible
    Expected Result: Content toggles visibility
    Evidence: .sisyphus/evidence/task-5-collapsible.{ext}

  Scenario: Shimmer during streaming
    Tool: Playwright
    Preconditions: streaming=true
    Steps:
      1. Verify shimmer effect visible
    Expected Result: Animation plays
    Evidence: .sisyphus/evidence/task-5-shimmer.{ext}

  Scenario: No render when empty
    Tool: Playwright
    Preconditions: thinking=""
    Steps:
      1. Verify component does not render
    Expected Result: Component is null/not in DOM
    Evidence: .sisyphus/evidence/task-5-empty.{ext}
  ```

  **Commit**: YES
  - Message: `feat(ui): add ThinkingBlock collapsible component`
  - Files: `src/components/StreamingChat/ThinkingBlock.vue`

---

- [ ] 6. **Create ToolCallCard.vue** — `src/components/StreamingChat/ToolCallCard.vue`

  **What to do**:
  - Create collapsible card for single tool call
  - Props: toolCall (ToolCall object)
  - Show: tool name, status badge, expandable args/results
  - Status badges: pending (gray), running (yellow), complete (green), error (red)
  - JSON syntax highlighting for args and results

  **Must NOT do**:
  - Don't show if toolCall is undefined

  **Recommended Agent Profile**:
  > **Category**: `visual-engineering`
  > **Reason**: UI component with styling
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T5, T7, T8)
  - **Blocks**: T7, T9
  - **Blocked By**: T3

  **References**:
  - `shadcn.io/ai/tool` - Tool component design
  - `src/components/Chat/Message.vue:1-85` - Existing styling to follow

  **Acceptance Criteria**:
  - [ ] Props: toolCall: ToolCall
  - [ ] Shows tool name with icon
  - [ ] Status badge with correct color (pending/running/complete/error)
  - [ ] Collapsible - expanded shows args and result
  - [ ] JSON syntax highlighting for args/results
  - [ ] Shows "Running..." indicator for streaming status

  **QA Scenarios**:
  ```
  Scenario: Shows tool name and status
    Tool: Playwright
    Preconditions: toolCall with name="search", status="running"
    Steps:
      1. Verify "search" text visible
      2. Verify status badge shows "running"
    Expected Result: Tool name and status displayed
    Evidence: .sisyphus/evidence/task-6-name-status.{ext}

  Scenario: Expandable args/results
    Tool: Playwright
    Preconditions: toolCall with args and result
    Steps:
      1. Verify args are hidden initially
      2. Click to expand
      3. Verify args JSON visible
      4. Verify result visible
    Expected Result: Args and result expandable
    Evidence: .sisyphus/evidence/task-6-expand.{ext}

  Scenario: Error state styling
    Tool: Playwright
    Preconditions: toolCall with status="error"
    Steps:
      1. Verify error badge color
    Expected Result: Red error badge
    Evidence: .sisyphus/evidence/task-6-error.{ext}
  ```

  **Commit**: YES
  - Message: `feat(ui): add ToolCallCard component`
  - Files: `src/components/StreamingChat/ToolCallCard.vue`

---

- [ ] 7. **Create ToolCallList.vue** — `src/components/StreamingChat/ToolCallList.vue`

  **What to do**:
  - Container for multiple ToolCallCards
  - Props: toolCalls (ToolCall[])
  - Render list of ToolCallCard components
  - Show count indicator if collapsed

  **Must NOT do**:
  - Don't render if toolCalls is empty

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: Simple container component
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T5, T6, T8)
  - **Blocks**: T9
  - **Blocked By**: T3, T6

  **References**:
  - `src/components/StreamingChat/ToolCallCard.vue` (T6)

  **Acceptance Criteria**:
  - [ ] Props: toolCalls: ToolCall[]
  - [ ] Renders ToolCallCard for each tool
  - [ ] Doesn't render if toolCalls is empty
  - [ ] Shows count if multiple tools

  **QA Scenarios**:
  ```
  Scenario: Renders multiple tool cards
    Tool: Playwright
    Preconditions: toolCalls array with 2 items
    Steps:
      1. Verify 2 ToolCallCards rendered
    Expected Result: 2 cards visible
    Evidence: .sisyphus/evidence/task-7-multiple.{ext}

  Scenario: Empty array doesn't render
    Tool: Playwright
    Preconditions: toolCalls=[]
    Steps:
      1. Verify no cards rendered
    Expected Result: Component not rendered
    Evidence: .sisyphus/evidence/task-7-empty.{ext}
  ```

  **Commit**: YES
  - Message: `feat(ui): add ToolCallList container component`
  - Files: `src/components/StreamingChat/ToolCallList.vue`

---

- [ ] 8. **Write unit tests for components** — `src/components/StreamingChat/*.test.ts`

  **What to do**:
  - Test ThinkingBlock collapsible behavior
  - Test ToolCallCard status display
  - Test ToolCallList rendering

  **Must NOT do**:
  - Don't test integration - separate task

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: TDD tests, straightforward
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T5, T6, T7)
  - **Blocks**: F1-F4
  - **Blocked By**: T5, T6, T7

  **Acceptance Criteria**:
  - [ ] Tests for ThinkingBlock collapse/expand
  - [ ] Tests for ToolCallCard status badge
  - [ ] Tests for ToolCallList empty state
  - [ ] All tests pass

  **QA Scenarios**:
  ```
  Scenario: All component tests pass
    Tool: Bash (bun test)
    Steps:
      1. Run bun test src/components/StreamingChat/
    Expected Result: All tests PASS
    Evidence: .sisyphus/evidence/task-8-tests.{ext}
  ```

  **Commit**: YES
  - Message: `test(components): add unit tests for StreamingChat components`
  - Files: `src/components/StreamingChat/*.test.ts`

---

- [ ] 9. **Create StreamingMessage.vue** — `src/components/StreamingChat/StreamingMessage.vue`

  **What to do**:
  - Individual message component with role-based styling
  - User messages: right-aligned, simple
  - Assistant messages: left-aligned, avatar, with ThinkingBlock and ToolCallList
  - Markdown rendering for content
  - Props: message (StreamingMessage)
  - Slots for custom content

  **Must NOT do**:
  - Don't duplicate existing Chat/Message styling exactly - improve

  **Recommended Agent Profile**:
  > **Category**: `visual-engineering`
  > **Reason**: Core message component with multiple sub-components
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T5, T6, T7, T8)
  - **Blocks**: T10
  - **Blocked By**: T3, T5, T6, T7

  **References**:
  - `src/components/Chat/Message.vue:1-85` - Existing message to improve upon
  - `shadcn.io/ai/message` - Message component design
  - `src/components/StreamingChat/ThinkingBlock.vue` (T5)
  - `src/components/StreamingChat/ToolCallList.vue` (T7)

  **Acceptance Criteria**:
  - [ ] Props: message: StreamingMessage
  - [ ] User messages: right-aligned with user styling
  - [ ] Assistant messages: left-aligned with avatar
  - [ ] Markdown renders in content
  - [ ] Shows ThinkingBlock when message.thinking exists
  - [ ] Shows ToolCallList when message.toolCalls exists
  - [ ] Shows streaming indicator when status === 'streaming'

  **QA Scenarios**:
  ```
  Scenario: User message right-aligned
    Tool: Playwright
    Preconditions: message with role="user"
    Steps:
      1. Verify message is right-aligned
    Expected Result: CSS class .message-right applied
    Evidence: .sisyphus/evidence/task-9-user.{ext}

  Scenario: Assistant message with thinking
    Tool: Playwright
    Preconditions: message with role="assistant", thinking="..."
    Steps:
      1. Verify ThinkingBlock visible
    Expected Result: ThinkingBlock rendered
    Evidence: .sisyphus/evidence/task-9-thinking.{ext}

  Scenario: Assistant message with tool calls
    Tool: Playwright
    Preconditions: message with role="assistant", toolCalls=[...]
    Steps:
      1. Verify ToolCallList visible
    Expected Result: ToolCallList rendered
    Evidence: .sisyphus/evidence/task-9-tools.{ext}

  Scenario: Markdown rendering
    Tool: Playwright
    Preconditions: message with content="**bold** text"
    Steps:
      1. Verify <strong>bold</strong> rendered
    Expected Result: Markdown parsed correctly
    Evidence: .sisyphus/evidence/task-9-markdown.{ext}

  Scenario: Streaming indicator
    Tool: Playwright
    Preconditions: message with status="streaming"
    Steps:
      1. Verify streaming indicator visible
    Expected Result: Cursor blink or similar
    Evidence: .sisyphus/evidence/task-9-streaming.{ext}
  ```

  **Commit**: YES
  - Message: `feat(ui): add StreamingMessage component`
  - Files: `src/components/StreamingChat/StreamingMessage.vue`

---

- [ ] 10. **Create StreamingChat.vue** — `src/components/StreamingChat/StreamingChat.vue`

  **What to do**:
  - Main chat container component
  - Scrollable message list (StreamingMessage items)
  - Auto-scroll to bottom on new messages
  - Input area with send button
  - Loading/disabled state while streaming
  - Empty state for no messages
  - v-model for messages array (optional)

  **Must NOT do**:
  - Don't add file upload
  - Don't add message branching

  **Recommended Agent Profile**:
  > **Category**: `visual-engineering`
  > **Reason**: Main container with complex scroll behavior
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with T11, T12)
  - **Blocks**: T11
  - **Blocked By**: T9

  **References**:
  - `src/components/Chat/Chat.vue:1-127` - Existing Chat to improve upon
  - `src/views/FlowDesigner/FlowDesigner.vue` - Scroll behavior reference

  **Acceptance Criteria**:
  - [ ] Props: modelValue?: StreamingMessage[]
  - [ ] Emits: update:modelValue, send
  - [ ] Scrollable message list
  - [ ] Auto-scroll to bottom on new message
  - [ ] Input textarea + send button
  - [ ] Send button disabled while streaming
  - [ ] Empty state when no messages
  - [ ] Enter to send, Shift+Enter for newline

  **QA Scenarios**:
  ```
  Scenario: Auto-scroll to bottom
    Tool: Playwright
    Preconditions: 10 messages (overflow)
    Steps:
      1. Send new message
      2. Verify scroll position at bottom
    Expected Result: Scroll === scrollHeight
    Evidence: .sisyphus/evidence/task-10-autoscroll.{ext}

  Scenario: Send button disabled during streaming
    Tool: Playwright
    Preconditions: isStreaming === true
    Steps:
      1. Verify send button is disabled
    Expected Result: Button has disabled attribute
    Evidence: .sisyphus/evidence/task-10-disabled.{ext}

  Scenario: Empty state
    Tool: Playwright
    Preconditions: messages === []
    Steps:
      1. Verify empty state message visible
    Expected Result: "Start a conversation" message
    Evidence: .sisyphus/evidence/task-10-empty.{ext}

  Scenario: Enter to send
    Tool: Playwright
    Preconditions: Input focused
    Steps:
      1. Type "Hello"
      2. Press Enter
    Expected Result: Message sent, input cleared
    Evidence: .sisyphus/evidence/task-10-enter.{ext}
  ```

  **Commit**: YES
  - Message: `feat(ui): add StreamingChat container component`
  - Files: `src/components/StreamingChat/StreamingChat.vue`

---

- [ ] 11. **Create StreamingChatView.vue** — `src/views/StreamingChatView/StreamingChatView.vue`

  **What to do**:
  - Page component for chat route
  - Integrates useStreamingChat composable
  - Layout: sidebar (optional) + main chat area
  - Model selector dropdown (optional)
  - Initialize composable on mount

  **Must NOT do**:
  - Don't add authentication

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: Page wrapper, straightforward
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with T10, T12)
  - **Blocks**: T12
  - **Blocked By**: T10

  **References**:
  - `src/views/ChatBox/ChatBox.vue:1-109` - Existing ChatBox to reference
  - `src/router/index.ts` - Router pattern

  **Acceptance Criteria**:
  - [ ] Uses useStreamingChat composable
  - [ ] Renders StreamingChat component
  - [ ] Route: /streaming-chat
  - [ ] Page layout matches project style

  **QA Scenarios**:
  ```
  Scenario: Page loads without error
    Tool: Playwright
    Steps:
      1. Navigate to /streaming-chat
    Expected Result: Page loads, no console errors
    Evidence: .sisyphus/evidence/task-11-load.{ext}

  Scenario: Composable initializes
    Tool: Playwright
    Steps:
      1. Navigate to /streaming-chat
      2. Check composable state
    Expected Result: isStreaming available
    Evidence: .sisyphus/evidence/task-11-init.{ext}
  ```

  **Commit**: YES
  - Message: `feat(view): add StreamingChatView page`
  - Files: `src/views/StreamingChatView/StreamingChatView.vue`

---

- [ ] 12. **Integration test with mock SSE** — `src/composables/useStreamingChat.integration.test.ts`

  **What to do**:
  - Full flow test: send message → receive tokens → receive tool events → complete
  - Verify UI updates correctly
  - Verify error recovery

  **Must NOT do**:
  - Don't test with real backend

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: Integration test, well-defined flow
  > **Skills**: none needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with T10, T11)
  - **Blocks**: F1-F4
  - **Blocked By**: T11

  **Acceptance Criteria**:
  - [ ] Test: sendMessage → token streaming → complete
  - [ ] Test: sendMessage → toolStart → toolEnd → complete
  - [ ] Test: error recovery
  - [ ] All tests pass

  **QA Scenarios**:
  ```
  Scenario: Full streaming flow
    Tool: Bash (bun test)
    Steps:
      1. Run integration test
    Expected Result: All integration tests PASS
    Evidence: .sisyphus/evidence/task-12-integration.{ext}
  ```

  **Commit**: YES
  - Message: `test(integration): add mock SSE integration tests`
  - Files: `src/composables/useStreamingChat.integration.test.ts`

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Read the plan end-to-end. For each "Must Have": verify implementation exists. For each "Must NOT Have": search codebase for forbidden patterns — reject with file:line if found.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run `tsc --noEmit` + linter + `bun test`. Review for `as any`/`@ts-ignore`, empty catches, console.log in prod, commented-out code, unused imports.
  Output: `Build [PASS/FAIL] | Lint [PASS/FAIL] | Tests [N pass/N fail] | Files [N clean/N issues] | VERDICT`

- [ ] F3. **Real Manual QA** — `unspecified-high` (+ `playwright` skill if UI)
  Start from clean state. Execute EVERY QA scenario from EVERY task — follow exact steps, capture evidence.
  Output: `Scenarios [N/N pass] | Integration [N/N] | Edge Cases [N tested] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  For each task: read "What to do", read actual diff. Verify 1:1 — everything in spec was built, nothing beyond spec was built.
  Output: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | Unaccounted [CLEAN/N files] | VERDICT`

---

## Commit Strategy

| Commit | Files | Description |
|--------|-------|-------------|
| 1 | `src/types/streaming-chat.d.ts` | Add streaming chat type definitions |
| 2 | `src/stores/chat.ts` | Add chat Pinia store |
| 3 | `src/composables/useStreamingChat.ts` | Add useStreamingChat composable |
| 4 | `src/composables/useStreamingChat.test.ts` | Add composable unit tests |
| 5 | `src/components/StreamingChat/ThinkingBlock.vue` | Add ThinkingBlock component |
| 6 | `src/components/StreamingChat/ToolCallCard.vue` | Add ToolCallCard component |
| 7 | `src/components/StreamingChat/ToolCallList.vue` | Add ToolCallList component |
| 8 | `src/components/StreamingChat/*.test.ts` | Add component unit tests |
| 9 | `src/components/StreamingChat/StreamingMessage.vue` | Add StreamingMessage component |
| 10 | `src/components/StreamingChat/StreamingChat.vue` | Add StreamingChat container |
| 11 | `src/views/StreamingChatView/StreamingChatView.vue` | Add StreamingChatView page |
| 12 | `src/composables/useStreamingChat.integration.test.ts` | Add integration tests |

---

## Success Criteria

### Verification Commands
```bash
bun test src/  # All tests pass
tsc --noEmit   # TypeScript compiles
```

### Final Checklist
- [ ] All "Must Have" present
- [ ] All "Must NOT Have" absent
- [ ] All tests pass
- [ ] TypeScript compiles without errors
- [ ] No console errors in browser
