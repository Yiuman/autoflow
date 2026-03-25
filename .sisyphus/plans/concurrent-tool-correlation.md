# Fix: 并发工具调用的 SSE 事件关联问题

## TL;DR

> **问题**: ReAct 一轮调用多个同名工具时（如 3 个 HTTP 请求），后端用 `toolName` 作为 `pendingTools` 的 key，导致结果错乱。前端也无法正确关联 `tool_end` 事件到对应的 `tool_start`。

> **解决**: 引入唯一的 `toolId`，贯穿后端 `pendingTools` → `StreamListener` → `AgentSSEEvent` → 前端 SSE 回调全链路。

---

## Context

### 原始问题 (用户提供)

```
tool_start (HTTP → Chongqing)
tool_start (HTTP → Wuhan)
tool_start (HTTP → Nanjing)
tool_end   (HTTP → nanjing result)   ← 结果错配
tool_end   (HTTP → Error)             ← 应该是 Chongqing
tool_end   (HTTP → Error)             ← 应该是 Wuhan
```

### 根因分析

| 层级 | 文件 | 问题 |
|------|------|------|
| **Agent** | `ReActAgent.java:231` | `pendingTools.put(req.name(), future)` — key 只是 `toolName`，同名工具会覆盖 |
| **Agent** | `ReActAgent.java:326` | `getToolResult(toolName)` — 并发时取到错误结果 |
| **Agent** | `ReActAgent.java:292` | `listener.onToolCallEnd(toolName, result)` — 未传递 toolId |
| **Listener** | `ChatStreamListener.java:77` | `currentToolCall` 单字段被覆盖 |
| **Listener** | `ChatStreamListener.java:84` | SSE `tool_start` 事件无 `toolId` 字段 |
| **Listener** | `ChatStreamListener.java:95` | SSE `tool_end` 事件无 `toolId` 字段 |
| **Model** | `AgentSSEEvent.java` | 缺少 `toolId` 字段 |
| **Frontend** | `ChatInputBar.vue:332` | `find(b => b.toolName === toolName)` — 并发时永远只匹配第一个 |

### 涉及文件

**Backend:**
- `autoflow-agent/src/main/java/io/autoflow/agent/ReActAgent.java`
- `autoflow-agent/src/main/java/io/autoflow/agent/StreamListener.java` (interface)
- `autoflow-app/src/main/java/io/autoflow/app/listener/ChatStreamListener.java`
- `autoflow-app/src/main/java/io/autoflow/app/model/sse/AgentSSEEvent.java`

**Frontend:**
- `autoflow-fe/src/api/chat.ts`
- `autoflow-fe/src/components/Chat/ChatInputBar.vue`

---

## Work Objectives

### Core Objective
修复并发工具调用的 SSE 事件关联，确保 `tool_start` 和 `tool_end` 通过唯一 `toolId` 正确配对。

### Concrete Deliverables

1. **后端** — `StreamListener` 接口添加 `toolId` 参数
2. **后端** — `AgentSSEEvent` 添加 `toolId` 字段
3. **后端** — `ReActAgent` 生成 UUID 作为 `toolId`，贯穿全链路
4. **后端** — `ChatStreamListener` 接收并传递 `toolId`
5. **前端** — `chat.ts` SSE 回调传递 `toolId`
6. **前端** — `ChatInputBar.vue` 用 `toolId` 而非 `toolName` 做关联

### Must Have
- [ ] 并发调用 3 个同名 HTTP 工具时，每个工具的 `tool_start`/`tool_end` 正确配对
- [ ] SSE 事件包含 `toolId` 字段
- [ ] 前端 UI 显示正确的工具结果

### Must NOT Have
- [ ] 不修改已有的 `StreamListener` 之外的其他实现（如果有的话需确认）
- [ ] 不改变 SSE 事件的基础结构（保持向后兼容，添加字段而非删除）

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES
- **Automated tests**: YES (after)
- **Framework**: Maven (Java), Vitest (Frontend)
- **Agent-Executed QA**: 模拟 3 个并发 HTTP 调用场景，验证结果正确关联

### QA Policy
Every task includes agent-executed QA scenarios:
- **Backend**: 写单元测试验证 `pendingTools` 用 `toolId` 作为 key
- **Frontend**: 写 QA scenario 验证并发 tool_start/tool_end 配对正确

---

## Execution Strategy

### Task Split (4 waves, maximizing parallelism)

```
Wave 1 (foundation — interfaces + model):
├── Task 1: Add toolId to StreamListener interface (backend contract)
├── Task 2: Add toolId to AgentSSEEvent model
└── Task 3: Add toolId to ChatSSECallbacks (frontend API contract)

Wave 2 (backend core — ReActAgent):
├── Task 4: ReActAgent — 生成 UUID 作为 toolId，修改 pendingTools key
└── Task 5: ReActAgent — 传递 toolId 到 onToolCallStart/End

Wave 3 (backend listener + frontend):
├── Task 6: ChatStreamListener — 接收 toolId，透传到 SSE 事件
└── Task 7: ChatInputBar.vue — 用 toolId 关联 tool_end 到正确的 block

Wave 4 (integration + tests):
├── Task 8: 集成测试 — 3 个并发 HTTP 工具调用
└── Task 9: 前端手动 QA 验证
```

### Dependency Matrix

| Task | Blocks | Blocked By |
|------|--------|------------|
| 1 | 2, 5 | — |
| 2 | 6 | — |
| 3 | 7 | — |
| 4 | 5 | 1 |
| 5 | 8 | 1, 4 |
| 6 | 8 | 2 |
| 7 | 9 | 3 |
| 8 | — | 5, 6, 7 |
| 9 | — | 8 |

---

## TODOs

---

- [ ] 1. **StreamListener 接口添加 toolId 参数**

  **What to do**:
  - 修改 `StreamListener.java` 中 `onToolCallStart` 和 `onToolCallEnd` 方法签名，添加 `toolId` 参数
  ```java
  void onToolCallStart(String toolId, String toolName, String arguments);
  void onToolCallEnd(String toolId, String toolName, Object result);
  ```
  - 检查是否有其他实现类（如 `ChatStreamListener`）需要同步修改

  **Must NOT do**:
  - 不要修改 `onThinking`、`onToken` 等其他方法签名
  - 不要添加新方法

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 接口修改，签名变更明确，影响范围可控
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Tasks 4, 5
  - **Blocked By**: None

  **References**:
  - `autoflow-agent/src/main/java/io/autoflow/agent/StreamListener.java:24-31` — 当前方法签名
  - `autoflow-app/src/main/java/io/autoflow/app/listener/ChatStreamListener.java:76-98` — 当前实现

  **Acceptance Criteria**:
  - [ ] `StreamListener` 接口两个方法签名包含 `String toolId` 参数
  - [ ] `mvn compile -pl autoflow-agent` → SUCCESS

---

- [ ] 2. **AgentSSEEvent 添加 toolId 字段**

  **What to do**:
  - 修改 `AgentSSEEvent.java`，添加 `toolId` 字段
  ```java
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public class AgentSSEEvent {
      private String type;
      private String content;
      private String toolId;      // 新增
      private String toolName;
      private String arguments;
      private Object result;
  }
  ```

  **Must NOT do**:
  - 不要删除现有字段
  - 不要修改字段类型

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的 Lombok 数据类字段添加
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Task 6
  - **Blocked By**: None

  **References**:
  - `autoflow-app/src/main/java/io/autoflow/app/model/sse/AgentSSEEvent.java:12-22` — 当前结构

  **Acceptance Criteria**:
  - [ ] `AgentSSEEvent` 包含 `toolId` 字段
  - [ ] `mvn compile -pl autoflow-app` → SUCCESS

---

- [ ] 3. **Frontend ChatSSECallbacks 添加 toolId**

  **What to do**:
  - 修改 `autoflow-fe/src/api/chat.ts` 中 `ChatSSECallbacks` 接口
  ```typescript
  export interface ChatSSECallbacks {
    onThinking?: (text: string) => void
    onToken?: (text: string) => void
    onToolStart?: (toolId: string, toolName: string, toolArgs: string) => void
    onToolEnd?: (toolId: string, toolName: string, result: any) => void
    onComplete?: (fullOutput: string) => void
    onError?: (message: string) => void
  }
  ```
  - 修改 `chatSSE` 函数中 `tool_start`/`tool_end` 的解析逻辑，传递 `toolId`

  **Must NOT do**:
  - 不要修改其他回调方法签名

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的接口和函数参数变更
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Task 7
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/api/chat.ts:37-44` — 当前 `ChatSSECallbacks` 定义
  - `autoflow-fe/src/api/chat.ts:81-89` — 当前 SSE 事件解析

  **Acceptance Criteria**:
  - [ ] `ChatSSECallbacks.onToolStart` 签名包含 `toolId: string` 参数
  - [ ] `ChatSSECallbacks.onToolEnd` 签名包含 `toolId: string` 参数
  - [ ] `npm run type-check` → SUCCESS

---

- [ ] 4. **ReActAgent 生成 UUID 并修改 pendingTools key**

  **What to do**:
  - 在 `ReActAgent.java` 中，使用 `UUID.randomUUID().toString()` 生成 `toolId`
  - 将 `pendingTools` 的 key 从 `String toolName` 改为 `String toolId`
  - 修改 `onCompleteToolCall` 回调中 `pendingTools.put(key, future)` 使用 `toolId` 作为 key

  ```java
  @Override
  public void onCompleteToolCall(CompleteToolCall completeToolCall) {
      ToolExecutionRequest req = completeToolCall.toolExecutionRequest();
      String toolId = UUID.randomUUID().toString();  // 生成唯一 ID
      listener.onToolCallStart(toolId, req.name(), req.arguments());
      
      CompletableFuture<Object> future = CompletableFuture.supplyAsync(() ->
              executeTool(req.name(), req.arguments())
      );
      pendingTools.put(toolId, future);  // 用 toolId 作为 key
  }
  ```

  **Must NOT do**:
  - 不要修改 `executeTool` 的实现逻辑
  - 不要修改 `processToolResults` 的错误处理逻辑

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 明确的 key 替换，逻辑简单
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 5
  - **Blocked By**: Task 1

  **References**:
  - `autoflow-agent/src/main/java/io/autoflow/agent/ReActAgent.java:224-232` — `onCompleteToolCall` 当前实现
  - `autoflow-agent/src/main/java/io/autoflow/agent/ReActAgent.java:38` — `pendingTools` 声明

  **Acceptance Criteria**:
  - [ ] `pendingTools` 使用 `toolId` (UUID) 作为 key
  - [ ] `mvn compile -pl autoflow-agent` → SUCCESS

---

- [ ] 5. **ReActAgent 传递 toolId 到 onToolCallEnd**

  **What to do**:
  - 修改 `processToolResults` 方法，通过 `toolId` 从 `pendingTools` 获取结果
  - 修改 `listener.onToolCallEnd(toolId, toolName, result)` 传递 `toolId`

  ```java
  private void processToolResults(List<ToolExecutionRequest> toolCalls, AgentContext context, StreamListener listener) {
      if (pendingTools.isEmpty()) {
          return;
      }

      CompletableFuture.allOf(pendingTools.values().toArray(new CompletableFuture[0])).join();

      // 需要建立 toolName -> toolId 的映射来找到对应的 pending future
      // 因为 processToolResults 拿到的是 toolCalls (List<ToolExecutionRequest>)
      // 需要修改这里逻辑：通过 toolId 查找而非 toolName
      
      for (ToolExecutionRequest request : toolCalls) {
          String toolId = findToolIdByRequest(request);  // 需要新方法或数据结构
          String toolName = request.name();
          Object result = pendingTools.get(toolId).get();  // 用 toolId 获取
          listener.onToolCallEnd(toolId, toolName, result);
          // ...
      }
  }
  ```

  **关键问题**: `processToolResults` 接收的是 `List<ToolExecutionRequest>`，但 `pendingTools` 现在用 `toolId` 做 key。需要追踪映射。

  **建议方案**: 在 `onCompleteToolCall` 时，同时将 `toolId -> toolName` 和 `toolId -> pendingFuture` 存入两个 map：
  ```java
  private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingTools = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> toolIdToName = new ConcurrentHashMap<>();  // 新增

  @Override
  public void onCompleteToolCall(CompleteToolCall completeToolCall) {
      ToolExecutionRequest req = completeToolCall.toolExecutionRequest();
      String toolId = UUID.randomUUID().toString();
      toolIdToName.put(toolId, req.name());  // 新增映射
      listener.onToolCallStart(toolId, req.name(), req.arguments());
      CompletableFuture<Object> future = CompletableFuture.supplyAsync(() ->
              executeTool(req.name(), req.arguments())
      );
      pendingTools.put(toolId, future);
  }
  ```

  然后在 `processToolResults` 中通过 `toolId` 查找：
  ```java
  for (ToolExecutionRequest request : toolCalls) {
      String toolId = findToolIdByName(request.name());  // 从 toolIdToName 反查
      Object result = pendingTools.get(toolId).get();
      listener.onToolCallEnd(toolId, request.name(), result);
      // ...
  }
  ```

  **Must NOT do**:
  - 不要在 `processToolResults` 中用 `toolName` 作为 `pendingTools` 的 key

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要理解两个 map 之间的映射关系，逻辑有一定复杂度
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 8
  - **Blocked By**: Tasks 1, 4

  **References**:
  - `autoflow-agent/src/main/java/io/autoflow/agent/ReActAgent.java:280-314` — `processToolResults` 当前实现
  - `autoflow-agent/src/main/java/io/autoflow/agent/ReActAgent.java:231` — `pendingTools.put`

  **Acceptance Criteria**:
  - [ ] `onToolCallEnd` 接收 `toolId` 参数
  - [ ] 并发调用 3 个同名 HTTP 工具时，每个 `tool_end` 携带正确的 `toolId`
  - [ ] `mvn test -pl autoflow-agent -Dtest=ReActIntegrationTest` → PASS

---

- [ ] 6. **ChatStreamListener 接收并传递 toolId 到 SSE 事件**

  **What to do**:
  - 修改 `onToolCallStart` 方法签名，接收 `toolId` 参数
  - 修改 `onToolCallEnd` 方法签名，接收 `toolId` 参数
  - 将 `toolId` 写入 `AgentSSEEvent` 并发送

  ```java
  @Override
  public void onToolCallStart(String toolId, String toolName, String arguments) {
      sendEvent("tool_start", AgentSSEEvent.builder()
              .type("tool_start")
              .toolId(toolId)          // 新增
              .toolName(toolName)
              .arguments(arguments)
              .build());
  }

  @Override
  public void onToolCallEnd(String toolId, String toolName, Object result) {
      sendEvent("tool_end", AgentSSEEvent.builder()
              .type("tool_end")
              .toolId(toolId)          // 新增
              .toolName(toolName)
              .result(result)
              .build());
  }
  ```

  - 删除不再需要的 `currentToolCall` 相关代码（`currentToolCall` 字段、`thinkingBuffer`、`contentBuffer` 等不受影响不需要改）

  **Must NOT do**:
  - 不要删除 `thinkingBuffer`、`contentBuffer` 等无关字段
  - 不要修改其他事件类型的处理逻辑

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 直接的方法签名修改和字段传递
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 8
  - **Blocked By**: Task 2

  **References**:
  - `autoflow-app/src/main/java/io/autoflow/app/listener/ChatStreamListener.java:76-98` — 当前实现

  **Acceptance Criteria**:
  - [ ] `tool_start` SSE 事件包含 `toolId` 字段
  - [ ] `tool_end` SSE 事件包含 `toolId` 字段
  - [ ] `mvn compile -pl autoflow-app` → SUCCESS

---

- [ ] 7. **ChatInputBar.vue 用 toolId 关联 tool_end**

  **What to do**:
  - 修改 `onToolStart` 回调，接收 `toolId` 参数并存储到 block
  ```typescript
  onToolStart: (toolId, toolName, args) => {
    lastEventWasToken = false
    chatStore.addBlock(assistantMsg.id, {
      type: MessageBlockType.TOOL,
      toolId,  // 使用传入的 toolId，而非生成新的
      toolName,
      arguments: JSON.parse(args),
      content: '',
      status: MessageBlockStatus.PENDING
    } as any)
  }
  ```
  - 修改 `onToolEnd` 回调，用 `toolId` 查找对应的 block
  ```typescript
  onToolEnd: (toolId, toolName, result) => {
    lastEventWasToken = false
    const blocks = chatStore.getBlocksByMessage(assistantMsg.id)
    const toolBlock = blocks.find(b => b.type === MessageBlockType.TOOL && b.toolId === toolId)  // 用 toolId 匹配
    if (toolBlock) {
      chatStore.updateBlock(toolBlock.id, {
        content: result,
        status: MessageBlockStatus.SUCCESS,
        metadata: {
          rawMcpToolResponse: {
            id: toolId,  // 使用传入的 toolId
            tool: { name: toolName, type: 'builtin' },
            status: 'done',
            response: result
          }
        }
      } as any)
    }
  }
  ```

  **Must NOT do**:
  - 不要在 `onToolStart` 中用 `uuid()` 生成新的 `toolId`（后端已生成）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的参数传递和查找条件变更
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 9
  - **Blocked By**: Task 3

  **References**:
  - `autoflow-fe/src/components/Chat/ChatInputBar.vue:318-347` — 当前 `onToolStart`/`onToolEnd` 实现

  **Acceptance Criteria**:
  - [ ] `onToolStart` 使用传入的 `toolId` 而非生成的 UUID
  - [ ] `onToolEnd` 用 `toolId === b.toolId` 查找 block
  - [ ] `npm run type-check` → SUCCESS

---

- [ ] 8. **集成测试：3 个并发 HTTP 工具调用**

  **What to do**:
  - 运行 `mvn test -pl autoflow-agent -Dtest=ReActIntegrationTest` 验证现有测试
  - 如果没有并发工具测试，创建简单的集成测试场景：
    1. 启动 autoflow-app
    2. 发送一个会触发 3 个并发 HTTP 请求的 prompt（如"查一下重庆、武汉、南京的天气"）
    3. 验证 SSE 事件流中：
       - 3 个 `tool_start` 事件，toolId 各不相同
       - 3 个 `tool_end` 事件，toolId 与对应的 `tool_start` 匹配
       - 结果正确分配（无错配）

  **QA Scenarios**:

  ```
  Scenario: 3 个并发 HTTP 工具调用正确关联
    Tool: Bash (curl)
    Preconditions: autoflow-app 运行在 localhost:8080
    Steps:
      1. POST /api/chat with input="查一下重庆、武汉、南京的天气" and sessionId
      2. Parse SSE stream, collect all tool_start and tool_end events
      3. Extract toolIds from tool_start events
      4. Match each tool_end event to its tool_start by toolId
      5. Verify 3 unique toolIds exist
      6. Verify each toolId has exactly one tool_start and one tool_end
    Expected Result: All 3 toolIds matched correctly, no mismatches
    Failure Indicators: toolId mismatch, missing tool_end, duplicate tool_end
    Evidence: .sisyphus/evidence/task-8-concurrent-tools.txt
  ```

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要集成测试环境和结果验证
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 9
  - **Blocked By**: Tasks 5, 6, 7

  **References**:
  - `autoflow-agent/src/test/java/io/autoflow/agent/ReActIntegrationTest.java` — 现有测试参考

  **Acceptance Criteria**:
  - [ ] 3 个并发 HTTP 调用产生 3 个不同的 `toolId`
  - [ ] 每个 `tool_end` 的 `toolId` 正确匹配对应的 `tool_start`
  - [ ] `mvn test -pl autoflow-agent` → ALL PASS

---

- [ ] 9. **前端手动 QA 验证**

  **What to do**:
  - 启动 `autoflow-fe` 开发服务器
  - 启动 `autoflow-app`
  - 在 UI 上发送"查一下重庆、武汉、南京的天气"
  - 验证 UI 中显示 3 个工具调用卡片，每个卡片：
    - 显示正确的城市参数
    - 显示正确的返回结果
    - 无结果错配（武汉卡片显示武汉结果）

  **QA Scenarios**:

  ```
  Scenario: 前端 UI 正确显示 3 个并发工具结果
    Tool: Playwright (frontend QA)
    Preconditions: autoflow-fe 运行在 localhost:5173, autoflow-app 运行在 localhost:8080
    Steps:
      1. Open browser, navigate to http://localhost:5173
      2. Create new chat session
      3. Send message: "查一下重庆、武汉、南京的天气"
      4. Wait for all tool_end events received
      5. Locate 3 tool result cards in DOM
      6. Extract tool arguments (city names) from each card
      7. Extract tool results from each card
      8. Verify: (Chongqing card shows Chongqing weather) AND (Wuhan card shows Wuhan weather) AND (Nanjing card shows Nanjing weather)
    Expected Result: Each city's card shows the correct weather for that city
    Failure Indicators: City/result mismatch, wrong result displayed, missing cards
    Evidence: .sisyphus/evidence/task-9-frontend-ui.png (screenshot)
  ```

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 手动 UI 验证需要真实环境
  - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocked By**: Task 8

  **Acceptance Criteria**:
  - [ ] 3 个工具卡片正确显示
  - [ ] 每个卡片结果与参数城市匹配
  - [ ] 无 UI 错误或异常

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Read all modified files. Verify:
  - `StreamListener` interface has `toolId` in method signatures
  - `AgentSSEEvent` has `toolId` field
  - `ReActAgent` generates UUID and uses as key
  - `ChatStreamListener` passes `toolId` to SSE events
  - `ChatInputBar.vue` uses `toolId` for correlation
  Output: `Must Have [N/N] | VERDICT`

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run `mvn compile -pl autoflow-agent,autoflow-app` + `npm run type-check` in autoflow-fe
  Output: `Build [PASS/FAIL] | Type-check [PASS/FAIL] | VERDICT`

- [ ] F3. **Integration Test** — `unspecified-high`
  Execute 3 concurrent HTTP tool scenario via SSE
  Output: `toolIds [N/N matched] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  For each task: verify "What to do" was implemented, no extra changes
  Output: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | VERDICT`

---

## Commit Strategy

- **1**: `fix(agent): add toolId for concurrent tool call correlation` — StreamListener.java, ReActAgent.java
- **2**: `fix(app): add toolId to AgentSSEEvent and ChatStreamListener` — AgentSSEEvent.java, ChatStreamListener.java
- **3**: `fix(fe): use toolId for tool_start/tool_end correlation` — chat.ts, ChatInputBar.vue
- **Pre-commit**: `mvn compile -pl autoflow-agent,autoflow-app`

---

## Success Criteria

### Verification Commands
```bash
# Backend compile
mvn compile -pl autoflow-agent,autoflow-app

# Backend test
mvn test -pl autoflow-agent

# Frontend type-check
cd autoflow-fe && npm run type-check

# Manual QA: SSE stream shows 3 different toolIds
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"input":"查一下重庆、武汉、南京的天气","sessionId":"test-session"}' \
  --no-buffer 2>&1 | grep toolId
```

### Final Checklist
- [ ] All 9 tasks complete
- [ ] `StreamListener` interface updated with `toolId`
- [ ] `AgentSSEEvent` model has `toolId` field
- [ ] `ReActAgent` generates UUID and uses as `pendingTools` key
- [ ] SSE events contain `toolId`
- [ ] Frontend correlates by `toolId` not `toolName`
- [ ] Concurrent 3-HTTP call test passes
- [ ] UI displays correct results per tool
