# 会话标题自动生成

## TL;DR

> **Quick Summary**: 在聊天完成时（onComplete），后端自动检测并异步生成会话标题
> 
> **Deliverables**:
> - `ChatSessionServiceImpl.generateTitle(sessionId)` — 查询问答内容，生成标题
> - `ChatStreamListener.onComplete()` — 检测并触发标题生成
> 
> **Estimated Effort**: Quick | **Parallel Execution**: NO (sequential)
> **Critical Path**: Task 1 → Task 2

---

## Context

### Original Request
用户希望实现会话标题的自动生成功能，对标业界主流 AI 问答产品（ChatGPT、Claude、豆包等）的实现方式。

### Agreed Approach
- **触发时机**: `ChatStreamListener.onComplete()` 时
- **生成方式**: 后端异步生成，用第一轮问答内容（用户消息 + AI 回复）生成标题
- **技术方案**: 复用现有 SSE 通道，不依赖 WebSocket

### Research Findings
- 业界主流采用乐观更新 + 异步 LLM 生成的混合策略
- autoflow 现有 `generateTitle()` 方法已实现但从未被调用
- 需要修改：注入 `ChatMessageService`，查询用户消息和 AI 回复

---

## Work Objectives

### Core Objective
在聊天完成时自动生成会话标题，存储到数据库，前端刷新时可获取最新标题。

### Concrete Deliverables
- 修改 `ChatSessionServiceImpl.generateTitle(sessionId)` 方法
- 修改 `ChatStreamListener.onComplete()` 调用标题生成

### Definition of Done
- [ ] POST /chat 调用后，标题自动生成并保存到 `af_chat_session` 表

### Must Have
- 异步执行，不阻塞 SSE complete 事件
- 检测 session.title 是否有值，避免重复生成

### Must NOT Have
- 不在前端 SSE 事件中附带标题（保持简单）
- 不改变现有 SSE 事件格式

---

## Verification Strategy

### QA Policy
- 后端测试：调用 chat 接口，验证数据库 session.title 有值
- 前端验证：刷新会话列表，标题正确显示

---

## Execution Strategy

### Tasks (Sequential - 2 tasks)

```
Task 1: 修改 ChatSessionServiceImpl — 注入 ChatMessageService，修改 generateTitle
Task 2: 修改 ChatStreamListener — onComplete 里调用 generateTitle

Critical Path: Task 1 → Task 2
```

---

## TODOs

---

- [x] 1. 修改 ChatSessionServiceImpl — 注入 ChatMessageService，修改 generateTitle

  **What to do**:
  1. 在构造函数注入 `ChatMessageService`
  2. 修改 `generateTitle(String sessionId)` 方法：
     - 先查 Session，判断 `title` 是否已有值，有则跳过
     - 查询用户第一条消息：`chatMessageService.findFirstUserMessage(sessionId)`
     - 查询 AI 回复第一条：`chatMessageService.findFirstAiMessage(sessionId)`
     - 用问答内容调用 `generateTitleFromLlm(firstUserMessage, firstAiResponse)`
     - 如果 LLM 返回为空，用 `getFallbackTitle(firstUserMessage)`
     - 保存标题到 Session

  **Must NOT do**:
  - 不要阻塞 SSE 完成事件
  - 不要在生成失败时重试多次

  **Recommended Agent Profile**:
  > **Category**: `unspecified-high` — Spring Boot 后端服务
  > **Skills**: Java, Spring Boot, MyBatis-Plus
  > - `Java`: 核心语言
  > - `Spring Boot`: 后端框架
  > - `MyBatis-Plus`: 数据库操作

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 2
  - **Blocked By**: None

  **References**:
  - `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatSessionServiceImpl.java` - 需要修改的文件
  - `autoflow-app/src/main/java/io/autoflow/app/service/ChatMessageService.java` - 查询消息的服务接口
  - `autoflow-app/src/main/java/io/autoflow/app/model/ChatSession.java` - Session 实体

  **Acceptance Criteria**:
  - [x] `ChatSessionServiceImpl` 成功注入 `ChatMessageService`
  - [x] `generateTitle(sessionId)` 方法内部先查 Session 判断是否有标题
  - [x] 方法内部查询用户消息和 AI 回复
  - [x] 标题生成后正确保存到 Session

  **QA Scenarios**:

  \`\`\`
  Scenario: generateTitle 保存标题到数据库
    Tool: Bash
    Preconditions: 数据库有 sessionId="test-title-1" 的会话，title 为空
    Steps:
      1. 调用 chat 接口完成一次对话
      2. 直接调用 generateTitle("test-title-1")
      3. 查询数据库 af_chat_session 表该 session 的 title 字段
    Expected Result: title 字段有值（LLM 生成或 fallback 截取）
    Failure Indicators: title 为 null 或空字符串
    Evidence: .sisyphus/evidence/task-1-title-saved.log
  \`\`\`

  **Commit**: YES
  - Message: `feat(chat): auto-generate session title on complete`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/service/impl/ChatSessionServiceImpl.java`

---

- [x] 2. 修改 ChatStreamListener — onComplete 里调用 generateTitle

  **What to do**:
  1. 在 `onComplete(String fullOutput)` 方法中
  2. 在保存 Session status="COMPLETED" 之前
  3. 检测 `session.getTitle() == null`
  4. 如果为空，用 `CompletableFuture.runAsync()` 异步调用 `chatSessionService.generateTitle(sessionId)`
  5. 不要等待生成完成，继续执行后续逻辑

  **Must NOT do**:
  - 不要在异步任务外等待标题生成
  - 不要传递 firstUserMessage 参数（方法内自己查）

  **Recommended Agent Profile**:
  > **Category**: `unspecified-high` — Spring Boot 后端服务
  > **Skills**: Java, Spring Boot
  > - `Java`: 核心语言
  > - `Spring Boot`: 后端框架

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: None
  - **Blocked By**: Task 1

  **References**:
  - `autoflow-app/src/main/java/io/autoflow/app/listener/ChatStreamListener.java` - 需要修改的文件
  - `autoflow-app/src/main/java/io/autoflow/app/rest/ChatController.java` - 参考 runAsyncChat 提交异步任务的方式

  **Acceptance Criteria**:
  - [x] `onComplete` 方法内检测 `session.getTitle() == null`
  - [x] 为空时异步调用 `chatSessionService.generateTitle(sessionId)`
  - [x] 不阻塞 SSE complete 事件发送

  **QA Scenarios**:

  ```
  Scenario: onComplete 触发标题生成
    Tool: Bash
    Preconditions: 后端运行中，存在 sessionId="test-complete-1"
    Steps:
      1. POST /chat 接口发送消息，sessionId="test-complete-1"
      2. 等待 SSE complete 事件返回
      3. 查询数据库 af_chat_session 表 title 字段
    Expected Result: SSE 响应完成后，数据库 title 已有值
    Failure Indicators: SSE 返回但 title 为空
    Evidence: .sisyphus/evidence/task-2-oncomplete-trigger.log
  ```

  **Commit**: YES
  - Message: `feat(chat): trigger title generation on complete event`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/listener/ChatStreamListener.java`
  - Pre-commit: `mvn test -Dtest=ChatSessionServiceImplTest`

---

## Final Verification Wave

- [x] F1. **功能验证** — 调用 chat 接口后查询 session.title 有值

---

## Success Criteria

### Verification Commands
```bash
# 1. 启动后端
cd autoflow-app && mvn spring-boot:run

# 2. 调用 chat 接口
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "test-session-1", "input": "你好，请介绍一下自己"}'

# 3. 查询数据库（或其他方式验证 session.title 有值）
```

### Final Checklist
- [x] `ChatSessionServiceImpl` 注入 `ChatMessageService`
- [x] `generateTitle(sessionId)` 内部查询用户消息和 AI 回复
- [x] `onComplete()` 检测 title 为空时异步调用 `generateTitle()`
- [ ] 标题正确保存到 `af_chat_session` 表
