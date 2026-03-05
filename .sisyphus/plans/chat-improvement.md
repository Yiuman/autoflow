# Chat功能完善计划

## TL;DR

> **快速总结**: 为现有Chat功能添加4个增强特性：Provider切换UI、代码块复制按钮、SSE错误重连、自动标题生成
> 
> **交付物**:
> - Provider下拉选择器组件
> - Markdown代码块复制功能
> - SSE重连机制
> - 自动会话标题生成
> 
> **预估工作量**: Short (约4-5小时)
> **并行执行**: YES - 3个Wave
> **关键路径**: 类型更新 → Provider/复制 → 重连/标题 → 集成测试

---

## Context

### 原始需求
用户希望完善Chat功能的UI和交互，具体包括:
1. Provider切换UI - 新增Provider下拉选择器
2. 代码块复制按钮 - 为Markdown代码块添加复制功能
3. 错误重连机制 - SSE断开后自动重连
4. 自动标题生成 - AI回复后自动生成会话标题

### 访谈总结
**关键讨论**:
- 项目已有完整后端API和类ChatGPT布局
- 使用Arco Design + md-editor-v3
- 需要Agent QA验证 (无测试框架)
- 用户选择"全部需要"

**研究发现的缺口**:
- 前端 `SendMessageRequest` 类型**缺少 provider 字段**
- 无SSE重连机制
- 无代码块复制按钮
- 无自动标题生成API

### Metis审查
**识别的缺口** (已解决):
- 前端类型不匹配后端 - 需添加 provider 字段
- 需要可复用的 composable (clipboard, SSE)
- 需要决定provider列表来源 - 使用hardcode
- 需要决定标题生成方式 - 使用客户端截取

---

## Work Objectives

### 核心目标
在现有Chat功能基础上增加4个增强特性，提升用户体验

### 具体交付物
1. **Provider切换组件** - ChatInput.vue新增下拉选择器
2. **代码块复制按钮** - MessageItem.vue新增复制功能
3. **SSE重连机制** - chat store增加重连逻辑
4. **自动标题生成** - 首次AI回复后自动更新会话标题

### 定义完成
- [x] Provider下拉出现在ChatInput左侧
- [x] 代码块悬停时显示复制按钮
- [x] SSE断开后自动重连3次 (1s→2s→4s)
- [x] AI回复后标题更新为用户消息前50字符

### Must Have
- [x] Provider下拉出现在ChatInput左侧
- [x] 代码块悬停时显示复制按钮
- [x] SSE断开后自动重连3次 (1s→2s→4s)
- [x] AI回复后标题更新为用户消息前50字符

### Must Have
- 类型安全 - 更新TS类型定义
- 错误反馈 - Toast通知
- 持久化 - localStorage存储

### Must NOT (Guardrails)

### Must Have
- 类型安全 - 更新TS类型定义
- 错误反馈 - Toast通知
- 持久化 - localStorage存储

### Must NOT (Guardrails)
- ❌ Provider配置管理 (API keys, endpoints)
- ❌ 代码执行或语法高亮主题
- ❌ 手动标题编辑UI
- ❌ Playwright测试 (项目无框架)

---

## Verification Strategy (MANDATORY)

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed.

### Test Decision
- **Infrastructure exists**: NO
- **Automated tests**: NO
- **Framework**: None
- **Agent-Executed QA**: 全部任务使用Agent QA验证

### QA Policy
Every task includes agent-executed QA scenarios:
- **Frontend/UI**: Playwright - 导航、交互、断言
- **API**: Bash curl - 验证请求格式
- **Evidence**: 截图和终端输出

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Foundation - 3 tasks并行):
├── Task 1: 更新前端类型定义 [quick]
├── Task 2: 创建useClipboard composable [quick]
└── Task 3: 创建useSSE reconnect composable [quick]

Wave 2 (Feature Implementation - 4 tasks并行):
├── Task 4: Provider下拉选择器 [quick]
├── Task 5: 代码块复制按钮 [quick]
├── Task 6: SSE重连集成 [quick]
└── Task 7: 自动标题生成 [quick]

Wave 3 (Integration - 1 task):
└── Task 8: 端到端测试和修复 [quick]
```

### Dependency Matrix
- **1-3**: — — 4-7
- **4-7**: 1,2,3 — 8
- **8**: 4,5,6,7 — END

---

## TODOs

- [x] 1. 更新前端Chat类型定义

  **What to do**:
  - 修改 `autoflow-fe/src/types/chat.d.ts`
  - 为 `SendMessageRequest` 添加 `provider?: string`
  - 添加 `Provider` 接口 (id, name, displayName)

  **Must NOT do**:
  - 不要修改后端类型

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的类型定义修改
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Tasks 4, 5, 6, 7
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/types/chat.d.ts` - 当前类型定义 (需修改)
  - `autoflow-app/.../SendMessageRequest.java` - 后端已有provider字段

  **Acceptance Criteria**:
  - [x] chat.d.ts 包含 provider?: string
  - [x] chat.d.ts 包含 Provider 接口
  - [x] TypeScript编译无错误

  **QA Scenarios**:
  - [x] chat.d.ts 包含 provider?: string
  - [x] chat.d.ts 包含 Provider 接口
  - [x] TypeScript编译无错误

  **QA Scenarios**:
  ```
  Scenario: 类型定义更新验证

  **QA Scenarios**:
  ```
  Scenario: 类型定义更新验证
    Tool: Bash
    Preconditions: 无
    Steps:
      1. cd autoflow-fe
      2. npx vue-tsc --noEmit --skipLibCheck 2>&1 | head -20
    Expected Result: 无类型错误 (允许现有错误)
    Evidence: .sisyphus/evidence/task-1-types-check.txt
  ```

  **Commit**: YES
  - Message: `feat(chat): add provider type definition`
  - Files: `autoflow-fe/src/types/chat.d.ts`

---

- [x] 2. 创建useClipboard composable

  **What to do**:
  - 创建 `autoflow-fe/src/composables/useClipboard.ts`
  - 实现 `copy(text: string): Promise<boolean>`
  - 处理浏览器兼容性
  - 返回 success/failure 状态

  **Must NOT do**:
  - 不要创建UI组件

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 纯逻辑 composable
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Task 5
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/composables/` - 现有composables目录
  - `navigator.clipboard` API 文档

  **Acceptance Criteria**:
  - [x] useClipboard.ts 存在
  - [x] copy函数返回Promise<boolean>
  - [x] 支持失败场景返回false

  **QA Scenarios**:
  - [x] 支持失败场景返回false

  **QA Scenarios**:
  ```
  Scenario: Clipboard composable创建验证

  **QA Scenarios**:
  ```
  Scenario: Clipboard composable创建验证
    Tool: Bash
    Preconditions: 无
    Steps:
      1. ls -la autoflow-fe/src/composables/useClipboard.ts
    Expected Result: 文件存在
    Evidence: .sisyphus/evidence/task-2-file-exists.txt
  ```

  **Commit**: YES
  - Message: `feat(chat): add useClipboard composable`
  - Files: `autoflow-fe/src/composables/useClipboard.ts`

---

- [x] 3. 创建useSSE reconnect composable

  **What to do**:
  - 创建 `autoflow-fe/src/composables/useSSE.ts`
  - 实现重连逻辑: 3次重试 (1s→2s→4s 指数退避)
  - 暴露状态: connecting, connected, error, retrying
  - 集成到现有chatApi.sendMessage

  **Must NOT do**:
  - 不要修改后端API

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要理解现有SSE实现并增强
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Task 6
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/api/chat.ts` - 现有SSE实现
  - `autoflow-fe/src/stores/chat.ts` - 现有store
  - `@microsoft/fetch-event-source` 文档

  **Acceptance Criteria**:
  - [x] useSSE.ts 存在
  - [x] 包含重试配置 (maxRetries: 3, delays: [1000, 2000, 4000])
  - [x] 暴露状态接口

  - [x] 暴露状态接口

  **QA Scenarios**:
  ```
  Scenario: SSE composable创建验证

  **QA Scenarios**:
  ```
  Scenario: SSE composable创建验证
    Tool: Bash
    Preconditions: 无
    Steps:
      1. ls -la autoflow-fe/src/composables/useSSE.ts
    Expected Result: 文件存在
    Evidence: .sisyphus/evidence/task-3-file-exists.txt
  ```

  **Commit**: YES
  - Message: `feat(chat): add useSSE composable with reconnection`
  - Files: `autoflow-fe/src/composables/useSSE.ts`

---

- [x] 4. Provider下拉选择器

  **What to do**:
  - 在 `ChatInput.vue` 添加ASelect组件
  - Providers: OpenAI, Gemini, Ollama, Qwen
  - 默认值: OpenAI (匹配后端默认)
  - 持久化到 localStorage key: 'chat-provider'
  - 发送消息时传递provider参数

  **Must NOT do**:
  - 不要添加provider配置页面

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的UI组件添加
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Task 1

  **References**:
  - `autoflow-fe/src/components/Chat/ChatInput.vue` - 现有输入组件
  - `autoflow-fe/src/api/chat.ts` - API调用
  - `autoflow-fe/src/stores/chat.ts` - store

  **Acceptance Criteria**:
  - [x] ChatInput.vue 包含Provider下拉
  - [x] 下拉显示4个选项
  - [x] 默认选中OpenAI
  - [x] localStorage存储选择值
  - [x] 发送消息时包含provider字段

  - [x] 发送消息时包含provider字段

  **QA Scenarios**:
  ```
  Scenario: Provider选择器验证

  **QA Scenarios**:
  ```
  Scenario: Provider选择器验证
    Tool: Playwright
    Preconditions: 启动开发服务器 localhost:5173
    Steps:
      1. Navigate to /chat
      2. Wait for chat input to load
      3. Locate provider dropdown (should be visible)
      4. Click dropdown to open
      5. Count options in dropdown
      6. Verify options include "OpenAI", "Gemini", "Ollama", "Qwen"
    Expected Result: 4个选项可见
    Evidence: .sisyphus/evidence/task-4-provider-dropdown.png

  Scenario: Provider持久化验证
    Tool: Bash
    Preconditions: 无
    Steps:
      1. cat ~/.local/share/mcp/chrome/Default/Local\ Storage/leveldb/*.log | grep chat-provider || echo "Not in browser storage yet"
    Expected Result: localStorage key存在
    Evidence: .sisyphus/evidence/task-4-persistence.txt
  ```

  **Commit**: YES (groups with 5,6,7)
  - Message: `feat(chat): add provider selector UI`
  - Files: `autoflow-fe/src/components/Chat/ChatInput.vue`

---

- [x] 5. 代码块复制按钮

  **What to do**:
  - 在 `MessageItem.vue` 的代码块添加复制按钮
  - 使用useClipboard composable
  - 成功显示 "Copied!" toast (2秒)
  - 失败显示 "Copy failed" toast
  - 按钮位置: 代码块header右侧 (悬停显示)

  **Must NOT do**:
  - 不要添加代码执行功能

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的UI增强
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 6, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Task 2

  **References**:
  - `autoflow-fe/src/components/Chat/MessageItem.vue` - 现有消息组件
  - `autoflow-fe/src/composables/useClipboard.ts` - Task 2创建

  **Acceptance Criteria**:
  - [x] 复制按钮在代码块悬停时显示
  - [x] 点击复制代码内容 (不含语言标识)
  - [x] 成功显示 toast
  - [x] 复制失败显示错误toast

  - [x] 复制失败显示错误toast

  **QA Scenarios**:
  ```
  Scenario: 复制按钮存在验证

  **QA Scenarios**:
  ```
  Scenario: 复制按钮存在验证
    Tool: Playwright
    Preconditions: 启动开发服务器, 已有消息包含代码块
    Steps:
      1. Navigate to /chat
      2. Send message "Show me Python code: print('hello')"
      3. Wait for AI response with code block
      4. Hover over code block
      5. Screenshot to verify copy button appears
    Expected Result: 截图显示复制按钮
    Evidence: .sisyphus/evidence/task-5-copy-button-hover.png

  Scenario: 复制功能验证
    Tool: Playwright
    Preconditions: 同上
    Steps:
      1. Click copy button
      2. Verify toast appears saying "Copied!"
      3. Paste in console to verify content
    Expected Result: toast显示且内容正确
    Evidence: .sisyphus/evidence/task-5-copy-success.png
  ```

  **Commit**: YES (groups with 4,6,7)
  - Message: `feat(chat): add code block copy button`
  - Files: `autoflow-fe/src/components/Chat/MessageItem.vue`

---

- [x] 6. SSE重连机制集成

  **What to do**:
  - 修改 `stores/chat.ts` 使用useSSE
  - 实现重连逻辑: 错误时自动重试3次
  - 显示状态: "Reconnecting..." (重连中)
  - 3次失败后显示 "Connection failed" + "Retry" 按钮

  **Must NOT do**:
  - 不要修改后端API

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要修改现有store逻辑
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Task 3

  **References**:
  - `autoflow-fe/src/stores/chat.ts` - 现有store
  - `autoflow-fe/src/composables/useSSE.ts` - Task 3创建
  - `autoflow-fe/src/api/chat.ts` - SSE调用

  **Acceptance Criteria**:
  - [x] 错误时自动重连
  - [x] 显示重连状态
  - [x] 3次失败后显示错误和重试按钮
  - [x] 重试按钮可重新发起请求

  - [x] 重试按钮可重新发起请求

  **QA Scenarios**:
  ```
  Scenario: SSE重连机制验证

  **QA Scenarios**:
  ```
  Scenario: SSE重连机制验证
    Tool: Playwright
    Preconditions: 启动开发服务器
    Steps:
      1. Navigate to /chat
      2. Send message "Hello"
      3. Simulate network error (DevTools → Network → Offline)
      4. Wait 5 seconds
      5. Verify reconnection attempts (check console for logs)
      6. Re-enable network
    Expected Result: 看到重连日志
    Evidence: .sisyphus/evidence/task-6-reconnect-log.txt
  ```

  **Commit**: YES (groups with 4,5,7)
  - Message: `feat(chat): add SSE reconnection mechanism`
  - Files: `autoflow-fe/src/stores/chat.ts`

---

- [x] 7. 自动标题生成

  **What to do**:
  - 在 `chat.ts` store 中处理 `message_end` 事件
  - 标题 = 用户第一条消息前50字符 (截断加...)
  - 通过API更新会话标题 (需确认后端API)
  - UI即时更新标题

  **Must NOT do**:
  - 不要添加LLM标题生成 (使用简单截取)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的字符串处理
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - N/A

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5, 6)
  - **Blocks**: Task 8
  - **Blocked By**: Task 1

  **References**:
  - `autoflow-fe/src/stores/chat.ts` - 现有store
  - `autoflow-app/.../ChatController.java` - 后端API (检查是否有更新会话的API)

  **Acceptance Criteria**:
  - [x] 首次AI回复后生成标题
  - [x] 标题为用户消息前50字符
  - [x] 标题通过API保存
  - [x] 侧边栏立即更新显示新标题

  - [x] 侧边栏立即更新显示新标题

  **QA Scenarios**:
  ```
  Scenario: 自动标题生成验证

  **QA Scenarios**:
  ```
  Scenario: 自动标题生成验证
    Tool: Playwright
    Preconditions: 启动开发服务器, 已有会话列表
    Steps:
      1. Navigate to /chat
      2. Click "New Chat"
      3. Verify title shows "New Chat"
      4. Send message "What is the capital of France?"
      5. Wait for AI response to complete
      6. Check sidebar session title
    Expected Result: 标题更新为 "What is the capital of France?" (或截断版本)
    Evidence: .sisyphus/evidence/task-7-title-update.png
  ```

  **Commit**: YES (groups with 4,5,6)
  - Message: `feat(chat): add auto title generation`
  - Files: `autoflow-fe/src/stores/chat.ts`

---

- [x] 8. 端到端测试和修复

  **What to do**:
  - 完整功能测试: 创建会话 → 发送消息 → 验证4个功能
  - 修复发现的问题
  - 验证UI一致性

  **Must NOT do**:
  - 不要添加新功能

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 综合测试和修复
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - N/A

  **Parallelization**:
  - **Can Run In Parallel**: NO (Sequential)
  - **Parallel Group**: Wave 3
  - **Blocks**: END
  - **Blocked By**: Tasks 4, 5, 6, 7

  **References**:
  - 所有之前Task的验收标准

  **Acceptance Criteria**:
  - [x] Provider选择正常
  - [x] 复制按钮功能正常
  - [x] SSE断开可重连
  - [x] 标题自动生成正常
  - [x] 无控制台错误

  - [x] 无控制台错误

  **QA Scenarios**:
  ```
  Scenario: 完整E2E测试

  **QA Scenarios**:
  ```
  Scenario: 完整E2E测试
    Tool: Playwright
    Preconditions: 启动开发服务器
    Steps:
      1. 创建新会话
      2. 验证Provider下拉存在且可选
      3. 发送包含代码的消息
      4. 验证代码块显示复制按钮
      5. 点击复制验证成功
      6. 验证回复后标题更新
      7. 验证无控制台错误
    Expected Result: 所有功能正常
    Evidence: .sisyphus/evidence/task-8-e2e.png
  ```

  **Commit**: YES
  - Message: `fix(chat): e2e fixes and improvements`
  - Files: 根据修复内容

---

## Final Verification Wave (MANDATORY)

> 4 review agents run in PARALLEL.

- [x] F1. **Plan Compliance Audit** — `oracle`
  验证所有Must Have已实现，Must NOT已排除
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT`

- [x] F2. **Code Quality Review** — `unspecified-high`
  运行 vue-tsc --noEmit，审查所有变更
  Output: `Build [PASS/FAIL] | Files [N clean/N issues] | VERDICT`

- [x] F3. **Real Manual QA** — `unspecified-high`
  执行每个Task的QA Scenarios
  Output: `Scenarios [N/N pass] | VERDICT`

- [x] F4. **Scope Fidelity Check** — `deep`

---

## Commit Strategy

---

## Commit Strategy

- **1**: `feat(chat): add provider type definition` - chat.d.ts
- **2**: `feat(chat): add useClipboard composable` - useClipboard.ts
- **3**: `feat(chat): add useSSE composable` - useSSE.ts
- **4-7**: `feat(chat): add UI enhancements` - ChatInput.vue, MessageItem.vue, chat.ts
- **8**: `fix(chat): e2e fixes` - 根据修复内容

---

## Success Criteria

### Verification Commands
```bash
cd autoflow-fe && npx vue-tsc --noEmit --skipLibCheck
# Expected: 无类型错误

npm run build
# Expected: 构建成功
```

### Final Checklist
- [x] 所有4个功能已实现
- [x] 无控制台错误
- [x] UI风格一致
- [x] 代码符合项目规范

## Success Criteria

