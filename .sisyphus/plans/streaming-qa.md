# Streaming Q&A Feature Implementation

## TL;DR

> **Quick Summary**: Replace the existing ChatBox with a streaming Q&A feature that supports real-time SSE streaming, collapsible thinking display with loading indicator, and tool call/result handling.
>
> **Deliverables**:
> - SSE client (`src/api/chatsse.ts`) for streaming chat responses
> - Pinia store (`src/stores/chat.ts`) for centralized chat state
> - Extended Message type with streaming fields
> - Updated Chat components with streaming support
> - ThinkingDisplay component (collapsible with loading)
> - ToolCallCard and ToolResultCard components
>
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Types → Store → SSE Client → Components → Integration

---

## Context

### Original Request
用户请求实现流式问答功能，包括 thinking、tool 事件处理等，参考 Cherry Studio 实现，仅实现前端功能。

### Interview Summary
**Key Discussions**:
- Backend API: 用户自己处理后端，前端只需对接 SSE 接口
- Thinking 显示: 折叠的 loading 显示，打开可看到 thinking 内容
- Tool 支持: 需要支持工具调用和结果展示
- Integration: 替换现有的 ChatBox

**Research Findings**:
- Cherry Studio 使用 callback composition 模式 (textCallbacks, thinkingCallbacks, toolCallbacks)
- BlockManager 处理 UI 更新的智能节流
- MessageBlock 有状态: PENDING, STREAMING, SUCCESS, ERROR
- Chunk 类型: text-start/delta/complete, thinking-start/delta/complete, tool-pending/in-progress/complete
- `@microsoft/fetch-event-source` 已安装在项目中

### Metis Review
**Identified Gaps** (addressed):
- ChatBox 是页面级组件，不是可复用组件 - 直接替换
- Messages 是本地 state，无 Pinia store - 需要新建 chat store
- Message 类型过于简单 - 需要扩展支持 streaming 字段
- flowsse.ts 使用不同端点 - 需要新建 chatsse.ts

---

## Work Objectives

### Core Objective
实现流式问答前端功能，替换现有 ChatBox，支持 SSE 流式响应、思考过程展示、工具调用展示。

### Concrete Deliverables
- `src/api/chatsse.ts` - SSE 流式客户端
- `src/stores/chat.ts` - Pinia chat 状态管理
- `src/types/chat.d.ts` - 扩展 Message 类型
- `src/components/Chat/ThinkingDisplay.vue` - 可折叠 thinking 显示
- `src/components/Chat/ToolCallCard.vue` - 工具调用卡片
- `src/components/Chat/ToolResultCard.vue` - 工具结果卡片
- `src/components/Chat/Message.vue` - 支持流式状态的消息组件
- `src/components/Chat/Chat.vue` - 集成流式功能
- `src/views/ChatBox/ChatBox.vue` - 接入新组件

### Definition of Done
- [ ] SSE 连接成功，消息在 100ms 内显示
- [ ] 文本增量渲染无闪烁
- [ ] Thinking 显示 loading 指示器，完成后折叠可展开
- [ ] Tool call 卡片在收到 tool_call 事件时显示
- [ ] Tool result 卡片在收到 tool_result 事件时显示
- [ ] done 事件标记消息完成，停止所有 loading 状态
- [ ] error 事件显示错误状态

### Must Have
- SSE 流式文本响应
- Thinking 折叠/展开显示
- Tool call 显示
- Tool result 显示
- 错误状态处理

### Must NOT Have (Guardrails)
- 不修改 flowsse.ts 和工作流执行 SSE
- 不添加对话历史/持久化（仅本地 state）
- 不实现多标签页同时聊天
- Thinking 不使用 markdown 渲染（纯文本）

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO
- **Automated tests**: None (no test framework in project)
- **Framework**: N/A
- **QA Policy**: Agent-executed QA scenarios only

### QA Policy
Every task includes agent-executed QA scenarios verified by running the deliverable:
- **UI Components**: Playwright - open browser, interact, assert DOM, screenshot
- **SSE Client**: Bash with curl/mocked EventSource - verify callback firing
- **Store**: Bash - verify state changes with console output

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Foundation - 4 tasks, can run in parallel):
├── Task 1: Extend Message type in chat.d.ts
├── Task 2: Create Pinia store useChatStore
├── Task 3: Create SSE client chatsse.ts
└── Task 4: Create ThinkingDisplay.vue

Wave 2 (Components - 4 tasks, after Wave 1):
├── Task 5: Create ToolCallCard.vue
├── Task 6: Create ToolResultCard.vue
├── Task 7: Extend Message.vue with streaming states
└── Task 8: Extend Chat.vue with streaming integration

Wave 3 (Integration - 2 tasks, after Wave 2):
├── Task 9: Update ChatBox.vue to use new components
└── Task 10: Integration testing and bug fixes
```

### Dependency Matrix

- **1, 2, 3, 4**: — — 5, 6, 7, 8, 1
- **5, 6, 7, 8**: 1, 2, 3, 4 — 9, 2
- **9**: 5, 6, 7, 8 — 10, 3
- **10**: 9 — — 3

### Agent Dispatch Summary

- **1**: **4** — T1 → `quick`, T2 → `unspecified-high`, T3 → `unspecified-high`, T4 → `visual-engineering`
- **2**: **4** — T5 → `visual-engineering`, T6 → `visual-engineering`, T7 → `visual-engineering`, T8 → `visual-engineering`
- **3**: **2** — T9 → `visual-engineering`, T10 → `deep`
- **FINAL**: **4** — F1 → `oracle`, F2 → `unspecified-high`, F3 → `unspecified-high`, F4 → `deep`

---

## TODOs

- [ ] 1. **Extend Message type for streaming support**

  **What to do**:
  - Modify `src/types/chat.d.ts` to add streaming fields:
    ```typescript
    interface Message {
      id?: string
      text?: string
      loading?: boolean
      error?: boolean
      user?: string
      location?: 'left' | 'right'
      streaming?: boolean        // Currently streaming
      thinkingContent?: string   // Reasoning content
      toolCalls?: ToolCall[]    // Pending tool calls
      toolResults?: ToolResult[] // Tool results
      status?: 'idle' | 'streaming' | 'done' | 'error'
    }

    interface ToolCall {
      id: string
      name: string
      arguments: Record<string, any>
    }

    interface ToolResult {
      id: string
      output: string
      isError?: boolean
    }
    ```
  - Keep backward compatibility with existing Message usage

  **Must NOT do**:
  - Delete existing fields (backward compatibility)
  - Use `content` instead of `text` (keep consistency)

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: Type definition only, no complex logic
  > - **Skills**: []
  > - **Skills Evaluated but Omitted**:
  >   - N/A - simple type edit

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: Tasks 5, 6, 7, 8 (components depend on types)
  - **Blocked By**: None (can start immediately)

  **References**:
  - `src/types/chat.d.ts` - Current Message type (simple, just text/loading/error/user/location)
  - `src/types/flow.d.ts:166-171` - ChatMessage type with MessageType enum
  - Cherry Studio `src/renderer/src/types/newMessage.ts` - BlockType and BlockStatus enums

  **Acceptance Criteria**:
  - [ ] TypeScript compiles without errors
  - [ ] Existing code using Message type still works
  - [ ] New streaming fields are optional (backward compatible)

  **QA Scenarios**:
  ```
  Scenario: Type extension doesn't break existing code
    Tool: Bash
    Preconditions: Existing Chat.vue uses Message type
    Steps:
      1. Run type-check: cd autoflow-fe && npx vue-tsc --noEmit
    Expected Result: No type errors related to Message
    Evidence: .sisyphus/evidence/task-1-type-check.txt
  ```

  **Commit**: YES
  - Message: `types(chat): extend Message for streaming`
  - Files: `src/types/chat.d.ts`

---

- [ ] 2. **Create Pinia chat store**

  **What to do**:
  - Create `src/stores/chat.ts` with:
    - State: `messages: Message[]`, `isStreaming: boolean`, `currentThinking: string`
    - Actions: `addMessage()`, `updateMessage()`, `streamContent()`, `setThinking()`, `addToolCall()`, `addToolResult()`, `completeStream()`, `errorStream()`, `clearMessages()`
    - Getters: `lastMessage`, `assistantMessages`
  - Follow existing store pattern from `src/stores/service.ts`

  **Must NOT do**:
  - Don't use Composition API style (use Options API like service.ts)
  - Don't add persistence (local state only)

  **Recommended Agent Profile**:
  > - **Category**: `unspecified-high`
    - Reason: Pinia store with streaming state management logic
  > - **Skills**: []
  > - **Skills Evaluated but Omitted**:
  >   - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4)
  - **Blocks**: Tasks 5, 6, 7, 8, 9
  - **Blocked By**: None

  **References**:
  - `src/stores/service.ts` - Pinia store pattern to follow
  - Cherry Studio `BlockManager.ts` - Block management with throttling
  - Cherry Studio `messageThunk.ts` - Stream processing logic

  **Acceptance Criteria**:
  - [ ] Store exports `useChatStore`
  - [ ] `messages` array managed correctly
  - [ ] Streaming state tracked
  - [ ] Tool calls/results stored

  **QA Scenarios**:
  ```
  Scenario: Store manages streaming message correctly
    Tool: Bash
    Preconditions: Store created
    Steps:
      1. Import store and verify exports
      2. Verify state shape matches Message type
    Expected Result: Store structure correct
    Evidence: .sisyphus/evidence/task-2-store.txt
  ```

  **Commit**: YES
  - Message: `store(chat): create useChatStore`
  - Files: `src/stores/chat.ts`

---

- [ ] 3. **Create SSE streaming client**

  **What to do**:
  - Create `src/api/chatsse.ts` using `@microsoft/fetch-event-source`
  - Handle SSE events:
    - `content`: Text chunk, call `onContent(text: string)`
    - `thinking`: Thinking chunk, call `onThinking(content: string)`
    - `tool_call`: Tool call, call `onToolCall(tool: ToolCall)`
    - `tool_result`: Tool result, call `onToolResult(result: ToolResult)`
    - `done`: Stream complete, call `onDone()`
    - `error`: Error occurred, call `onError(message: string)`
  - Export `createChatSSE(options)` function
  - Support AbortController for cancellation
  - Follow pattern from `src/views/FlowDesigner/flowsse.ts`

  **Must NOT do**:
  - Don't use `/executions/sse` endpoint (that's for workflow)
  - Don't block on streaming (must be async non-blocking)

  **Recommended Agent Profile**:
  > - **Category**: `unspecified-high`
    - Reason: SSE client with multiple event handlers
  > - **Skills**: []
  > - **Skills Evaluated but Omitted**:
  >   - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4)
  - **Blocks**: Tasks 7, 8, 9, 10
  - **Blocked By**: None

  **References**:
  - `src/views/FlowDesigner/flowsse.ts` - SSE client pattern to follow
  - `@microsoft/fetch-event-source` - Library already installed
  - Cherry Studio `handleToolCallChunk.ts` - Tool call handling

  **Acceptance Criteria**:
  - [ ] SSE client created with all event handlers
  - [ ] AbortController support works
  - [ ] Event types match backend specification

  **QA Scenarios**:
  ```
  Scenario: SSE client fires correct callback on content event
    Tool: Bash
    Preconditions: SSE endpoint returns test events
    Steps:
      1. Mock EventSource or use test endpoint
      2. Verify onContent callback fires
    Expected Result: Callback receives correct text
    Evidence: .sisyphus/evidence/task-3-sse-content.txt

  Scenario: SSE client fires correct callback on thinking event
    Tool: Bash
    Preconditions: SSE endpoint returns thinking event
    Steps:
      1. Mock EventSource with thinking event
      2. Verify onThinking callback fires
    Expected Result: Callback receives thinking content
    Evidence: .sisyphus/evidence/task-3-sse-thinking.txt

  Scenario: SSE client cancels on abort
    Tool: Bash
    Preconditions: SSE connection active
    Steps:
      1. Call abort() on controller
      2. Verify connection closes
    Expected Result: No further callbacks after abort
    Evidence: .sisyphus/evidence/task-3-sse-abort.txt
  ```

  **Commit**: YES
  - Message: `api(chatsse): create SSE streaming client`
  - Files: `src/api/chatsse.ts`

---

- [ ] 4. **Create ThinkingDisplay component**

  **What to do**:
  - Create `src/components/Chat/ThinkingDisplay.vue`
  - Props: `content: string`, `loading: boolean`
  - Display:
    - When `loading=true`: Show animated loading indicator (spinner or pulsing dots)
    - When `content` exists: Show collapsible section with thinking content
    - Expand/collapse on click
  - Use Arco Design components for consistency
  - Follow existing component styling patterns

  **Must NOT do**:
  - Don't use markdown rendering (plain text only)
  - Don't show if both loading=false and content empty

  **Recommended Agent Profile**:
  > - **Category**: `visual-engineering`
    - Reason: UI component with animations and state handling
  > - **Skills**: [`frontend-design`]
  > - `frontend-design`: Match existing Chat component styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3)
  - **Blocks**: Tasks 7, 8 (used in Message.vue and Chat.vue)
  - **Blocked By**: None

  **References**:
  - `src/components/Chat/Message.vue` - Component styling to match
  - `src/components/Chat/Chat.vue` - Container patterns
  - Cherry Studio thinking display pattern

  **Acceptance Criteria**:
  - [ ] Loading indicator shows when `loading=true`
  - [ ] Thinking content collapsible when `content` exists
  - [ ] Click toggles expand/collapse
  - [ ] Matches existing component styling

  **QA Scenarios**:
  ```
  Scenario: ThinkingDisplay shows loading indicator
    Tool: Playwright
    Preconditions: Component created
    Steps:
      1. Open component with :loading="true" :content="''"
      2. Verify loading indicator visible
    Expected Result: Spinner or pulsing dots visible
    Evidence: .sisyphus/evidence/task-4-loading.png

  Scenario: ThinkingDisplay collapses/expands on click
    Tool: Playwright
    Preconditions: Component with content
    Steps:
      1. Open component with :loading="false" :content="'thinking text'"
      2. Verify collapsed by default
      3. Click to expand
      4. Verify content visible
      5. Click to collapse
      6. Verify content hidden
    Expected Result: Toggle works correctly
    Evidence: .sisyphus/evidence/task-4-collapse.gif
  ```

  **Commit**: YES
  - Message: `component(ThinkingDisplay): create collapsible thinking`
  - Files: `src/components/Chat/ThinkingDisplay.vue`

---

- [ ] 5. **Create ToolCallCard component**

  **What to do**:
  - Create `src/components/Chat/ToolCallCard.vue`
  - Props: `toolCall: ToolCall`
  - Display:
    - Tool name prominently
    - Arguments in code block or formatted view
    - Status indicator (pending/running/success/error)
  - Use Arco Design Card component
  - Show tool call in a visually distinct card

  **Must NOT do**:
  - Don't execute tool (just display)
  - Don't allow user to modify tool call

  **Recommended Agent Profile**:
  > - **Category**: `visual-engineering`
    > - Reason: UI card component with tool display
  > - **Skills**: [`frontend-design`]
  > - `frontend-design`: Match existing styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8)
  - **Blocks**: Task 9 (used in ChatBox)
  - **Blocked By**: Task 1 (types), Task 2 (store), Task 3 (SSE client)

  **References**:
  - Cherry Studio `ToolMessageBlock` type
  - `src/components/Chat/Message.vue` - Styling reference

  **Acceptance Criteria**:
  - [ ] Tool name displayed prominently
  - [ ] Arguments shown in readable format
  - [ ] Status indicator works

  **QA Scenarios**:
  ```
  Scenario: ToolCallCard displays tool information
    Tool: Playwright
    Preconditions: Component created
    Steps:
      1. Open with :toolCall="{ id: '1', name: 'search', arguments: { query: 'test' } }"
      2. Verify tool name 'search' visible
      3. Verify arguments displayed
    Expected Result: Tool info clearly visible
    Evidence: .sisyphus/evidence/task-5-toolcard.png
  ```

  **Commit**: YES
  - Message: `component(ToolCallCard): create tool call card`
  - Files: `src/components/Chat/ToolCallCard.vue`

---

- [ ] 6. **Create ToolResultCard component**

  **What to do**:
  - Create `src/components/Chat/ToolResultCard.vue`
  - Props: `toolResult: ToolResult`
  - Display:
    - Tool result content
    - Success/error status styling
    - Error results in red/warning style
  - Use Arco Design components

  **Must NOT do**:
  - Don't allow editing of result
  - Don't auto-scroll to result (let user scroll)

  **Recommended Agent Profile**:
  > - **Category**: `visual-engineering`
    > - Reason: UI card component with result display
  > - **Skills**: [`frontend-design`]
  > - `frontend-design`: Match existing styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 7, 8)
  - **Blocks**: Task 9
  - **Blocked By**: Task 1 (types), Task 2 (store), Task 3 (SSE client)

  **References**:
  - Cherry Studio tool result display
  - `src/components/Chat/Message.vue` - Styling reference

  **Acceptance Criteria**:
  - [ ] Result content displayed
  - [ ] Error styling for isError=true
  - [ ] Success styling for normal results

  **QA Scenarios**:
  ```
  Scenario: ToolResultCard shows success result
    Tool: Playwright
    Preconditions: Component created
    Steps:
      1. Open with :toolResult="{ id: '1', output: 'search results here' }"
      2. Verify output visible
    Expected Result: Result displayed in normal style
    Evidence: .sisyphus/evidence/task-6-result-success.png

  Scenario: ToolResultCard shows error result
    Tool: Playwright
    Preconditions: Component created
    Steps:
      1. Open with :toolResult="{ id: '1', output: 'error message', isError: true }"
      2. Verify error styling applied
    Expected Result: Error styling (red/warning)
    Evidence: .sisyphus/evidence/task-6-result-error.png
  ```

  **Commit**: YES
  - Message: `component(ToolResultCard): create tool result card`
  - Files: `src/components/Chat/ToolResultCard.vue`

---

- [ ] 7. **Extend Message.vue with streaming support**

  **What to do**:
  - Modify `src/components/Chat/Message.vue` to:
    - Display `ThinkingDisplay` when `thinkingContent` exists or `loading` with thinking
    - Display `ToolCallCard` for each tool call in `toolCalls`
    - Display `ToolResultCard` for each tool result in `toolResults`
    - Show streaming cursor or indicator when `streaming=true`
    - Use `status` prop for visual states
  - Update template to include new components

  **Must NOT do**:
  - Don't break existing non-streaming usage
  - Don't remove markdown rendering for text

  **Recommended Agent Profile**:
  > - **Category**: `visual-engineering`
    > - Reason: Component composition with multiple child components
  > - **Skills**: [`frontend-design`]
  > - `frontend-design`: Maintain consistent styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 8)
  - **Blocks**: Task 9
  - **Blocked By**: Task 1 (types), Task 4 (ThinkingDisplay), Task 5, 6 (ToolCards)

  **References**:
  - `src/components/Chat/Message.vue` - Current implementation
  - `src/components/Chat/ThinkingDisplay.vue` - To be imported
  - `src/components/Chat/ToolCallCard.vue` - To be imported
  - `src/components/Chat/ToolResultCard.vue` - To be imported

  **Acceptance Criteria**:
  - [ ] Existing messages render correctly
  - [ ] Streaming messages show ThinkingDisplay
  - [ ] Tool calls and results display
  - [ ] Streaming indicator shows when streaming=true

  **QA Scenarios**:
  ```
  Scenario: Message shows thinking during stream
    Tool: Playwright
    Preconditions: Extended component
    Steps:
      1. Open with :thinkingContent="'thinking...'" :status="'streaming'"
      2. Verify ThinkingDisplay appears
      3. Verify streaming indicator visible
    Expected Result: Thinking visible with stream indicator
    Evidence: .sisyphus/evidence/task-7-streaming.png

  Scenario: Message shows tool cards
    Tool: Playwright
    Preconditions: Extended component
    Steps:
      1. Open with toolCalls and toolResults
      2. Verify ToolCallCards appear
      3. Verify ToolResultCards appear
    Expected Result: All tool cards visible
    Evidence: .sisyphus/evidence/task-7-toolcards.png
  ```

  **Commit**: YES
  - Message: `component(Message): extend with streaming`
  - Files: `src/components/Chat/Message.vue`

---

- [ ] 8. **Extend Chat.vue with streaming integration**

  **What to do**:
  - Modify `src/components/Chat/Chat.vue` to:
    - Import and use `useChatStore`
    - Connect send button to SSE streaming
    - Handle message send → create user message → start SSE stream
    - Update assistant message incrementally as SSE events arrive
    - Show loading state during streaming
  - Follow existing patterns for message input

  **Must NOT do**:
  - Don't break existing non-streaming usage
  - Don't hardcode SSE endpoint

  **Recommended Agent Profile**:
  > - **Category**: `visual-engineering`
    > - Reason: Integration component connecting store and SSE
  > - **Skills**: [`frontend-design`]
  > - `frontend-design`: Match existing styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 7)
  - **Blocks**: Task 9
  - **Blocked By**: Task 2 (store), Task 3 (SSE client)

  **References**:
  - `src/components/Chat/Chat.vue` - Current implementation
  - `src/stores/chat.ts` - Store to integrate
  - `src/api/chatsse.ts` - SSE client to integrate
  - `src/views/FlowDesigner/flowsse.ts` - SSE integration pattern

  **Acceptance Criteria**:
  - [ ] User message sent and displayed
  - [ ] SSE stream starts on send
  - [ ] Assistant message updates incrementally
  - [ ] Loading state shown during stream

  **QA Scenarios**:
  ```
  Scenario: Send message starts SSE stream
    Tool: Playwright
    Preconditions: Extended Chat component
    Steps:
      1. Type message in input
      2. Click send
      3. Verify user message appears
      4. Verify SSE connection initiated
    Expected Result: User message visible, SSE started
    Evidence: .sisyphus/evidence/task-8-send.gif
  ```

  **Commit**: YES
  - Message: `component(Chat): extend with streaming`
  - Files: `src/components/Chat/Chat.vue`

---

- [ ] 9. **Update ChatBox.vue integration**

  **What to do**:
  - Modify `src/views/ChatBox/ChatBox.vue` to:
    - Use new `Chat.vue` with streaming support
    - Remove old non-streaming Chat component usage
    - Ensure model selection and prompt config still work
    - Wire up the new streaming chat

  **Must NOT do**:
  - Don't change the layout significantly
  - Don't break existing model selection

  **Recommended Agent Profile**:
  > - **Category**: `visual-engineering`
    > - Reason: Integration of existing ChatBox with new Chat
  > - **Skills**: [`frontend-design`]
  > - `frontend-design`: Match existing ChatBox layout

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (with Task 10)
  - **Blocks**: Task 10
  - **Blocked By**: Tasks 5, 6, 7, 8

  **References**:
  - `src/views/ChatBox/ChatBox.vue` - Current implementation
  - `src/components/Chat/Chat.vue` - New component to use
  - `src/components/ExpressInput/ExpressInput.vue` - Prompt input

  **Acceptance Criteria**:
  - [ ] ChatBox uses new Chat component
  - [ ] Model selection still functional
  - [ ] Prompt config still functional
  - [ ] Streaming works end-to-end

  **QA Scenarios**:
  ```
  Scenario: ChatBox renders with new Chat
    Tool: Playwright
    Preconditions: Updated ChatBox
    Steps:
      1. Navigate to ChatBox page
      2. Verify Chat component renders
      3. Verify model selector visible
    Expected Result: Page renders correctly
    Evidence: .sisyphus/evidence/task-9-chatbox.png
  ```

  **Commit**: YES
  - Message: `views(ChatBox): integrate new components`
  - Files: `src/views/ChatBox/ChatBox.vue`

---

- [ ] 10. **Integration testing and bug fixes**

  **What to do**:
  - Full integration testing
  - Fix any issues found during testing
  - Verify error handling works
  - Verify abort (cancel) works
  - Verify edge cases (empty thinking, tool timeout, etc.)

  **Must NOT do**:
  - Don't add new features
  - Don't refactor existing working code

  **Recommended Agent Profile**:
  > - **Category**: `deep`
    > - Reason: Integration debugging and fixes
  > - **Skills**: [`systematic-debugging`]
    - `systematic-debugging`: Use systematic approach for bug fixes

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (with Task 9)
  - **Blocks**: Final verification
  - **Blocked By**: Task 9

  **References**:
  - All previous components and their references
  - Cherry Studio streaming error handling patterns

  **Acceptance Criteria**:
  - [ ] SSE streaming works end-to-end
  - [ ] Thinking display works
  - [ ] Tool call/result works
  - [ ] Error states handled
  - [ ] Abort works

  **QA Scenarios**:
  ```
  Scenario: Full streaming Q&A flow
    Tool: Playwright
    Preconditions: All components integrated
    Steps:
      1. Send a message that triggers thinking
      2. Verify thinking shows with loading
      3. Verify thinking collapses when content arrives
      4. Verify final response displays
    Expected Result: Complete flow works
    Evidence: .sisyphus/evidence/task-10-full-flow.gif

  Scenario: Error handling
    Tool: Playwright
    Preconditions: SSE error scenario
    Steps:
      1. Trigger error condition
      2. Verify error message displayed
      3. Verify retry option available
    Expected Result: Error handled gracefully
    Evidence: .sisyphus/evidence/task-10-error.png
  ```

  **Commit**: YES (if fixes needed)
  - Message: `fix: integration issues`
  - Files: Various if needed

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — `oracle`
- [ ] F2. **Code Quality Review** — `unspecified-high`
- [ ] F3. **Real Manual QA** — `unspecified-high`
- [ ] F4. **Scope Fidelity Check** — `deep`

---

## Commit Strategy

- **1**: `types(chat): extend Message for streaming` — src/types/chat.d.ts
- **2**: `store(chat): create useChatStore` — src/stores/chat.ts
- **3**: `api(chatsse): create SSE streaming client` — src/api/chatsse.ts
- **4**: `component(ThinkingDisplay): create collapsible thinking` — src/components/Chat/ThinkingDisplay.vue
- **5**: `component(ToolCallCard): create tool call card` — src/components/Chat/ToolCallCard.vue
- **6**: `component(ToolResultCard): create tool result card` — src/components/Chat/ToolResultCard.vue
- **7**: `component(Message): extend with streaming` — src/components/Chat/Message.vue
- **8**: `component(Chat): extend with streaming` — src/components/Chat/Chat.vue
- **9**: `views(ChatBox): integrate new components` — src/views/ChatBox/ChatBox.vue
- **10**: `fix: integration issues` — various if needed

---

## Success Criteria

### Verification Commands
```bash
# Type check
cd autoflow-fe && npm run type-check  # Expected: no errors

# Build
cd autoflow-fe && npm run build  # Expected: BUILD SUCCESS

# Dev server
cd autoflow-fe && npm run dev  # Expected: VITE dev server starts
```

### Final Checklist
- [ ] All Must Have items present
- [ ] All Must NOT Have items absent
- [ ] SSE client handles all event types
- [ ] Thinking collapses/expands correctly
- [ ] Tool cards display correctly
- [ ] Error states handled
