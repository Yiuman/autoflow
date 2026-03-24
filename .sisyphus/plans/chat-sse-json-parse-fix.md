# Chat SSE 数据格式修复计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan.

**Goal:** 修复前端chat.ts中thinking和token事件的SSE数据解析问题，使其与tool_start/tool_end事件处理方式一致

**Architecture:** 后端发送AgentSSEEvent对象（序列化为JSON），前端需要对thinking和token事件进行JSON.parse以获取content字段

**Tech Stack:** TypeScript, @microsoft/fetch-event-source

---

## 问题分析

### 当前代码 (chat.ts:66-89)

```typescript
case 'thinking':
  callbacks.onThinking?.(message.data)  // ❌ 直接使用，可能是JSON字符串
  break
case 'token':
  callbacks.onToken?.(message.data)      // ❌ 直接使用，可能是JSON字符串
  break
case 'tool_start': {
  const data = JSON.parse(message.data)  // ✅ 正确解析
  callbacks.onToolStart?.(data.toolName, data.arguments)
  break
}
case 'tool_end': {
  const data = JSON.parse(message.data)   // ✅ 正确解析
  callbacks.onToolEnd?.(data.toolName, data.result)
  break
}
```

### 问题

后端 `ChatStreamListener.java` 发送的SSE事件数据：
- `thinking`: `AgentSSEEvent { type: "thinking", content: "..." }`
- `token`: `AgentSSEEvent { type: "token", content: "..." }`

Spring的 `SseEmitter` 会将Java对象序列化为JSON字符串。前端收到的 `message.data` 是**JSON字符串**，需要 `JSON.parse()` 才能获取 `.content` 字段。

当前 `thinking` 和 `token` 事件直接使用 `message.data`，导致：
- `callbacks.onThinking(message.data)` 传入的是整个JSON字符串
- `callbacks.onToken(message.data)` 传入的是整个JSON字符串
- 最终页面显示的内容不正确

---

## TODOs

- [x] 1. **修复 chat.ts 中 thinking 事件的 JSON 解析**

  **What to do**:
  修改 `autoflow-fe/src/api/chat.ts` 第67-69行：

  ```typescript
  case 'thinking': {
    const data = JSON.parse(message.data)
    callbacks.onThinking?.(data.content)
    break
  }
  ```

  **Must NOT do**:
  - 不修改其他事件处理逻辑
  - 不改变回调函数的参数类型

  **References**:
  - `autoflow-fe/src/api/chat.ts:67-69` - 需要修改的代码位置

  **Acceptance Criteria**:
  
  \`\`\`
  Scenario: thinking 事件正确解析 JSON
    Tool: Playwright
    Preconditions: 后端运行 + 前端dev server运行
    Steps:
      1. 打开 ChatBox 页面
      2. 输入 "请解释为什么天空是蓝色的"
      3. 发送消息
      4. 观察 thinking 内容是否正确显示（而不是显示 {"type":"thinking","content":"..."}）
    Expected Result: thinking 内容正常显示，不是JSON字符串
    Evidence: .sisyphus/evidence/task-1-thinking-parse.webp
  \`\`\`

  **Commit**: YES
  - Message: `fix(chat): parse JSON for thinking SSE event`
  - Files: `autoflow-fe/src/api/chat.ts`

- [x] 2. **修复 chat.ts 中 token 事件的 JSON 解析**

  **What to do**:
  修改 `autoflow-fe/src/api/chat.ts` 第70-72行：

  ```typescript
  case 'token': {
    const data = JSON.parse(message.data)
    callbacks.onToken?.(data.content)
    break
  }
  ```

  **Must NOT do**:
  - 不修改其他事件处理逻辑

  **References**:
  - `autoflow-fe/src/api/chat.ts:70-72` - 需要修改的代码位置

  **Acceptance Criteria**:
  
  \`\`\`
  Scenario: token 事件正确解析 JSON
    Tool: Playwright
    Preconditions: 后端运行 + 前端dev server运行
    Steps:
      1. 打开 ChatBox 页面
      2. 输入 "你好"
      3. 发送消息
      4. 观察 assistant 的回复是否正确流式显示
    Expected Result: 回复内容正常显示，不是JSON字符串
    Evidence: .sisyphus/evidence/task-2-token-parse.webp
  \`\`\`

  **Commit**: YES
  - Message: `fix(chat): parse JSON for token SSE event`
  - Files: `autoflow-fe/src/api/chat.ts`

- [x] 3. **验证 complete 和 error 事件的处理**

  **What to do**:
  检查 `complete` 和 `error` 事件的处理方式是否正确。

  当前代码：
  ```typescript
  case 'complete':
    callbacks.onComplete?.(message.data)
    break
  case 'error':
    callbacks.onError?.(message.data)
    break
  ```

  后端发送的 `AgentSSEEvent` 对象：
  - `complete`: `{ type: "complete", content: fullOutput }`
  - `error`: `{ type: "error", content: errorMessage }`

  **问题**: `complete` 和 `error` 事件也是发送 `AgentSSEEvent` 对象，应该同样需要 JSON.parse。

  修改为：
  ```typescript
  case 'complete': {
    const data = JSON.parse(message.data)
    callbacks.onComplete?.(data.content)
    break
  }
  case 'error': {
    const data = JSON.parse(message.data)
    callbacks.onError?.(data.content)
    break
  }
  ```

  **References**:
  - `autoflow-fe/src/api/chat.ts:83-88` - 需要修改的代码位置

  **Acceptance Criteria**:
  
  \`\`\`
  Scenario: complete 事件正确解析
    Tool: Playwright
    Preconditions: 后端运行 + 前端dev server运行
    Steps:
      1. 发送一条消息
      2. 等待回复完成
      3. 观察 complete 后消息状态是否正确更新
    Expected Result: 消息状态正确更新为 done
    Evidence: .sisyphus/evidence/task-3-complete-parse.webp

  Scenario: error 事件正确解析
    Tool: Playwright  
    Preconditions: 后端运行 + 前端dev server运行
    Steps:
      1. 发送一条消息触发错误（如发送空消息）
      2. 观察错误信息是否正确显示
    Expected Result: 错误信息正常显示
    Evidence: .sisyphus/evidence/task-4-error-parse.webp
  \`\`\`

  **Commit**: YES
  - Message: `fix(chat): parse JSON for complete and error SSE events`
  - Files: `autoflow-fe/src/api/chat.ts`

- [x] 4. **运行前端类型检查**

  **What to do**:
  确保修改后 TypeScript 编译通过

  Run: `cd autoflow-fe && npm run build` 或 `npm run type-check`
  
  Expected: 无编译错误

  **Commit**: NO (如果前面已提交)

- [x] 5. **集成测试**

  **What to do**:
  启动后端和前端，执行完整的聊天流程测试

  \`\`\`
  Scenario: 完整聊天流程
    Tool: Playwright
    Preconditions: 后端运行在 localhost:8080 + 前端dev server运行
    Steps:
      1. 启动后端: cd autoflow-app && mvn spring-boot:run
      2. 启动前端: cd autoflow-fe && npm run dev
      3. 打开浏览器访问 http://localhost:5173/chat
      4. 输入 "你好，请自我介绍"
      5. 发送消息
      6. 观察:
         - thinking 内容正确显示
         - token 逐步流式显示回复
         - tool_start/tool_end 正确显示工具调用
         - complete 后回复完成
    Expected Result: 所有SSE事件正确处理，聊天正常进行
    Evidence: .sisyphus/evidence/final-chat-flow.webp
  \`\`\`

---

## 最终验证

- [x] F1. **代码审查** — 确保所有SSE事件处理都使用 JSON.parse

  Run: `grep -n "JSON.parse" autoflow-fe/src/api/chat.ts`
  Expected: thinking, token, tool_start, tool_end, complete, error 都有 JSON.parse

- [x] F2. **功能测试** — 完整的聊天流程测试

---

## Success Criteria

1. `thinking` 事件的 content 内容正确显示
2. `token` 事件的 content 内容正确流式显示
3. `tool_start`/`tool_end` 事件正常工作
4. `complete` 事件正确触发
5. `error` 事件正确显示错误信息
6. TypeScript 编译通过
7. 完整聊天流程测试通过
