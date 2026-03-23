# Draft: Cherry Studio Style ChatBox Streaming Implementation

## Requirements (confirmed)
- **Core objective**: Implement streaming Q&A ChatBox like Cherry Studio
- **Backend**: Existing SSE endpoint at `POST /chat` with events: thinking, token, tool_start, tool_end, complete, error
- **Request format**: `{ sessionId: string, input: string }`
- **Event format**: `{ type, content, toolName?, arguments?, result? }`
- **UI style**: Cherry Studio 样式 (React but we adapt patterns to Vue 3)
- **Framework**: Vue 3 (not React) - adapt Cherry Studio patterns

## Backend Event Reference (Confirmed)
```java
// ChatStreamListener.java events:
onThinking(String thinking)      -> event: "thinking", content: thinking
onToken(String token)         -> event: "token", content: token
onToolCallStart(name, args)   -> event: "tool_start", toolName: name, arguments: args
onToolCallEnd(name, result)   -> event: "tool_end", toolName: name, result: result
onComplete(fullOutput)         -> event: "complete", content: fullOutput
onError(Throwable)             -> event: "error", content: errorMessage
```

## Technical Decisions

### SSE Client Pattern
- Use `@microsoft/fetch-event-source` (already in use for flowsse.ts)
- Follow same pattern as `FlowDesigner/flowsse.ts`

### State Management
- Create Pinia store `useChatStore` for messages and streaming state
- Pattern: like `useServiceStore` but for chat

### Message Types (Enhanced)
```typescript
interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  thinking?: string
  toolCalls?: ToolCall[]
  status: 'idle' | 'streaming' | 'complete' | 'error'
  createdAt: number
}

interface ToolCall {
  id: string
  name: string
  status: 'pending' | 'running' | 'complete' | 'error'
  arguments?: string
  result?: any
}
```

## Cherry Studio Patterns to Adapt (from research)

### 1. StreamProcessingService Pattern
```typescript
// Callback-driven streaming
interface StreamCallbacks {
  onTextStart?: () => void
  onTextChunk?: (text: string) => void
  onTextComplete?: (text: string) => void
  onThinkingStart?: () => void
  onThinkingChunk?: (text: string) => void
  onThinkingComplete?: (text: string) => void
  onToolStart?: (tool: ToolCall) => void
  onToolEnd?: (tool: ToolCall) => void
  onComplete?: () => void
  onError?: (error: string) => void
}
```

### 2. UI Patterns
- Thinking: collapsible section with brain icon, shimmer during streaming
- Tool Calls: collapsible cards with status badges
- Messages: left (assistant) / right (user) aligned
- Streaming: typing cursor effect

## Existing Files to Modify
- `autoflow-fe/src/components/Chat/Chat.vue` - Main chat container
- `autoflow-fe/src/components/Chat/Message.vue` - Message bubble
- `autoflow-fe/src/views/ChatBox/ChatBox.vue` - Page view

## New Files to Create
- `autoflow-fe/src/api/chat.ts` - Chat API client
- `autoflow-fe/src/stores/chat.ts` - Pinia chat store
- `autoflow-fe/src/composables/useChatSSE.ts` - SSE composable
- `autoflow-fe/src/components/Chat/ThinkingBlock.vue` - Thinking indicator
- `autoflow-fe/src/components/Chat/ToolCallCard.vue` - Tool call display

## Scope Boundaries
- IN: Streaming chat UI, SSE integration, thinking display, tool call display
- OUT: File uploads, multi-session, authentication, persistence

## Open Questions
1. Session management: single session or multi-session?
2. Persistence: localStorage or just memory?
3. Thinking display: always show or collapsible?

## Test Strategy
- TDD: YES
- Framework: vitest (existing in project)
- QA: Agent-executed Playwright for UI verification
