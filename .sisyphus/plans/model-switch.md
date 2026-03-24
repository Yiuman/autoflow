# Plan: autoflow-fe Chat 模型切换功能

## TL;DR

> **Quick Summary**: 在 chat 对话模块中添加模型切换功能，支持每个话题独立选择 AI 模型
> 
> **Deliverables**:
> - 后端: `/api/models` 接口 + chat SSE 支持 modelId
> - 前端: 模型选择下拉框 + per-topic 模型持久化
> 
> **Estimated Effort**: Medium (前后端一起)
> **Parallel Execution**: YES - 可并行开发
> **Critical Path**: 后端 model API → 前端 API 层 → 前端 UI

---

## Context

### Original Request
在 autoflow-fe 前端项目的 chat 对话模块中增加一个切换模型的功能

### Interview Summary
**Key Discussions**:
- 模型列表: 从后端 API `/api/models` 获取
- 模型选择: Per-topic 独立保存 (每个话题可以选择不同模型)
- 切换影响: 保留当前对话，新消息使用新模型
- 后端状态: chat 接口需要支持 modelId 参数 (一起开发)
- 测试: 需要 vitest 单元/集成测试

**Research Findings**:
- Tech: Vue 3 + TypeScript + Arco Design Vue + Pinia
- 现有 `selectedModelId` 在 chat store 但没有 setter
- TagSelector.vue 是 a-select 的参考模式
- chatSSE() 目前只发 `{ input }`，需改为 `{ input, modelId }`
- 现有 CRUD 框架可参考用于 /api/models

---

## Work Objectives

### Core Objective
实现 per-topic 模型切换功能，允许用户在 chat 对话中为每个话题选择不同的 AI 模型

### Concrete Deliverables
- [ ] 后端 `/api/models` 接口 (CRUD 框架)
- [ ] 后端 chat SSE 支持 `modelId` 参数
- [ ] 前端 `src/api/model.ts` 模型列表 API
- [ ] 前端 Topic 类型添加 `modelId` 字段
- [ ] 前端 ChatInputBar.vue 添加模型选择下拉框
- [ ] 前端 chatSSE() 发送 `modelId`
- [ ] vitest 测试用例

### Definition of Done
- [ ] `curl /api/models` 返回模型列表
- [ ] 发送 chat 时带 modelId，后端正确处理
- [ ] UI 下拉框显示模型列表，可选择并切换
- [ ] 切换模型后，新消息使用对应模型
- [ ] 刷新页面后，话题保持其模型选择
- [ ] `bun test` 所有测试通过

### Must Have
- Per-topic 模型持久化
- 模型列表从后端动态获取
- 切换模型不影响当前对话历史

### Must NOT Have (Guardrails)
- 不做全应用的全局模型默认
- 不做模型切换中的流式中断处理
- 不做模型权限控制 (后端负责)

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: YES (vitest in package.json)
- **Automated tests**: YES (vitest)
- **Framework**: vitest

### QA Policy
Every task MUST include agent-executed QA scenarios. Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — 基础定义 + 后端 API):
├── Task 1: 后端 - 定义 Model 实体和 CRUD API [backend]
├── Task 2: 后端 - Chat SSE 支持 modelId 参数 [backend]
├── Task 3: 前端 - Topic 类型添加 modelId 字段 [frontend-types]
└── Task 4: 前端 - 创建 src/api/model.ts [frontend-api]

Wave 2 (After Wave 1 — 前端 UI + 集成):
├── Task 5: 前端 - ChatInputBar 添加模型选择组件 [frontend-ui]
├── Task 6: 前端 - chatSSE() 支持 modelId [frontend-api]
├── Task 7: 前端 - Topic 创建时默认模型 [frontend]
└── Task 8: 前端 - vitest 测试 [testing]

Wave FINAL (After ALL tasks):
├── Task F1: 端到端集成验证
└── Task F2: 计划合规审计
```

### Dependency Matrix

| Task | Depends On | Blocks |
|------|------------|--------|
| 1 (Backend Model API) | — | 4 |
| 2 (Backend Chat modelId) | — | 6 |
| 3 (Topic modelId field) | — | 5, 6, 7 |
| 4 (Frontend model API) | 1 | 5 |
| 5 (ChatInputBar UI) | 3, 4 | — |
| 6 (chatSSE modelId) | 2, 3 | — |
| 7 (Topic default model) | 3 | — |
| 8 (Tests) | 5, 6 | — |
| F1 (E2E Verification) | 5, 6, 7, 8 | — |

---

## TODOs

- [ ] 1. **后端 - Model 实体和 CRUD API**

  **What to do**:
  - 在后端项目中创建 Model 实体 (参考现有实体如 Topic/Message)
  - 使用 CRUD 框架生成 `/api/models` 接口
  - 实现 GET /api/models 返回模型列表 `[{id, name}]`
  - 确保接口遵循现有 CRUD 模式

  **Must NOT do**:
  - 不要添加模型权限控制
  - 不要添加模型元数据 (先只返回 id 和 name)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: [`backend`, `crud-framework`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: Task 4 (前端 model API)
  - **Blocked By**: None

  **References**:
  - `backend/src/entity/` - 现有实体结构参考
  - `backend/src/crud/` - CRUD 框架使用方式

  **Acceptance Criteria**:
  - [ ] `curl /api/models` 返回 `[{id, name}]` 格式
  - [ ] 后端代码编译通过

  **QA Scenarios**:

  ```
  Scenario: 获取模型列表 API
    Tool: Bash (curl)
    Preconditions: 后端服务运行中
    Steps:
      1. curl -X GET http://localhost:端口/api/models
      2. 解析返回的 JSON 数组
    Expected Result: 返回 `[{id: "string", name: "string"}]` 格式的数组
    Evidence: .sisyphus/evidence/task-1-api-list.{ext}

  Scenario: API 错误处理
    Tool: Bash (curl)
    Preconditions: 后端服务运行中
    Steps:
      1. curl -X GET http://localhost:端口/api/models (带错误token)
    Expected Result: 返回 401 或适当错误码
    Evidence: .sisyphus/evidence/task-1-api-error.{ext}
  ```

  **Commit**: YES
  - Message: `feat(backend): add Model entity and /api/models CRUD API`
  - Files: `backend/src/entity/Model.ts`, `backend/src/crud/model.ts`

---

- [ ] 2. **后端 - Chat SSE 支持 modelId**

  **What to do**:
  - 修改 chat SSE 端点接收 `modelId` 参数
  - 将 `modelId` 存储在 Message 中或传递给 AI 服务
  - 确保不传 modelId 时使用默认模型

  **Must NOT do**:
  - 不要改变 SSE 响应格式 (保持兼容)
  - 不要添加模型切换中的流式中断

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: [`backend`, `sse`, `ai-service`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4)
  - **Blocks**: Task 6 (前端 chatSSE)
  - **Blocked By**: None

  **References**:
  - `backend/src/sse/chat.ts` 或类似 - 现有 chat SSE 实现
  - `backend/src/service/ai.ts` - AI 服务调用方式

  **Acceptance Criteria**:
  - [ ] 发送 chat 时带 modelId 参数，后端正确接收
  - [ ] 不带 modelId 时使用默认模型 (不报错)

  **QA Scenarios**:

  ```
  Scenario: 带 modelId 发送消息
    Tool: Bash (curl)
    Preconditions: 后端服务运行中，modelId=xxx 存在
    Steps:
      1. curl -X POST /api/chat/sse -d '{"input":"hello","modelId":"xxx"}'
    Expected Result: 后端正确处理请求，不返回错误
    Evidence: .sisyphus/evidence/task-2-chat-with-model.{ext}

  Scenario: 不带 modelId 发送消息
    Tool: Bash (curl)
    Preconditions: 后端服务运行中
    Steps:
      1. curl -X POST /api/chat/sse -d '{"input":"hello"}'
    Expected Result: 使用默认模型，正常返回
    Evidence: .sisyphus/evidence/task-2-chat-no-model.{ext}
  ```

  **Commit**: YES
  - Message: `feat(backend): support modelId parameter in chat SSE`
  - Files: `backend/src/sse/chat.ts`

---

- [ ] 3. **前端 - Topic 类型添加 modelId 字段**

  **What to do**:
  - 在 `src/types/chat.ts` 的 Topic 接口中添加 `modelId?: string` 字段
  - 确保类型定义与其他字段一致

  **Must NOT do**:
  - 不要删除现有字段
  - 不要改变 Topic 的其他结构

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`frontend`, `typescript`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4)
  - **Blocks**: Tasks 5, 6, 7
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/types/chat.ts:Topic` - Topic 类型定义位置

  **Acceptance Criteria**:
  - [ ] Topic 类型包含 `modelId?: string`
  - [ ] `tsc --noEmit` 通过

  **QA Scenarios**:

  ```
  Scenario: 类型检查
    Tool: Bash
    Preconditions: TypeScript 配置正确
    Steps:
      1. cd autoflow-fe && npx tsc --noEmit
    Expected Result: 无类型错误
    Evidence: .sisyphus/evidence/task-3-type-check.{ext}
  ```

  **Commit**: YES
  - Message: `feat(chat): add modelId field to Topic type`
  - Files: `autoflow-fe/src/types/chat.ts`

---

- [ ] 4. **前端 - 创建 src/api/model.ts**

  **What to do**:
  - 创建 `src/api/model.ts` 文件
  - 实现 `fetchModels(): Promise<Model[]>` 函数
  - 使用现有 API 模式 (参考 `src/api/chat.ts`)
  - 定义 Model 类型接口

  **Must NOT do**:
  - 不要添加缓存逻辑 (让调用方处理)
  - 不要添加 Model 选择逻辑

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`frontend`, `api`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3)
  - **Blocks**: Task 5 (ChatInputBar UI)
  - **Blocked By**: Task 1 (后端 Model API)

  **References**:
  - `autoflow-fe/src/api/chat.ts` - API 调用模式
  - `autoflow-fe/src/types/chat.ts` - 类型定义位置

  **Acceptance Criteria**:
  - [ ] `fetchModels()` 返回 `Model[]`
  - [ ] `tsc --noEmit` 通过

  **QA Scenarios**:

  ```
  Scenario: 获取模型列表 (mock)
    Tool: Bash (node)
    Preconditions: API mock 或后端可用
    Steps:
      1. node -e "import('./src/api/model.ts').then(m => m.fetchModels().then(console.log))"
    Expected Result: 返回模型数组
    Evidence: .sisyphus/evidence/task-4-api-call.{ext}

  Scenario: API 错误处理
    Tool: Bash (node)
    Preconditions: 后端不可用
    Steps:
      1. node -e "import('./src/api/model.ts').then(m => m.fetchModels().catch(e => console.error(e.message)))"
    Expected Result: 抛出错误，不静默失败
    Evidence: .sisyphus/evidence/task-4-api-error.{ext}
  ```

  **Commit**: YES
  - Message: `feat(api): add model.ts for model list API`
  - Files: `autoflow-fe/src/api/model.ts`

---

- [ ] 5. **前端 - ChatInputBar 添加模型选择组件**

  **What to do**:
  - 在 `ChatInputBar.vue` 的 `bottom-bar` 区域添加模型选择下拉框
  - 使用 Arco Design `a-select` 组件 (参考 TagSelector 模式)
  - 实现:
    - 从 `/api/models` 加载模型列表
    - 显示当前选择的模型名称
    - 选择后更新 Topic 的 `modelId`
  - 布局: 放在输入框左侧或工具栏区域

  **Must NOT do**:
  - 不要改变输入框的核心功能
  - 不要在消息流传输中切换模型

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend`, `vue`, `arco-design`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8)
  - **Blocks**: None
  - **Blocked By**: Tasks 3, 4 (Topic 类型 + model API)

  **References**:
  - `autoflow-fe/src/components/Chat/ChatInputBar.vue` - 目标文件
  - `autoflow-fe/src/components/TagSelector/TagSelector.vue` - a-select 模式参考
  - `autoflow-fe/src/stores/chat.ts` - store 中的 Topic 操作

  **Acceptance Criteria**:
  - [ ] 下拉框显示在 ChatInputBar 中
  - [ ] 可选择模型并更新 Topic.modelId
  - [ ] 加载状态显示正确
  - [ ] 错误状态处理正确

  **QA Scenarios**:

  ```
  Scenario: 模型下拉框渲染
    Tool: Playwright
    Preconditions: ChatInputBar 组件已挂载
    Steps:
      1. 打开 ChatBox 页面
      2. 等待 .chat-input-bar 或类似选择器出现
      3. 查找 .model-selector 或 a-select 组件
    Expected Result: 下拉框可见，placeholder 或默认模型显示
    Evidence: .sisyphus/evidence/task-5-ui-render.png

  Scenario: 选择模型
    Tool: Playwright
    Preconditions: 模型列表已加载
    Steps:
      1. 点击模型下拉框
      2. 等待选项列表出现
      3. 点击第二个模型选项
    Expected Result: 下拉框显示选中模型名称
    Evidence: .sisyphus/evidence/task-5-select-model.png

  Scenario: 加载错误
    Tool: Playwright
    Preconditions: /api/models 返回 500
    Steps:
      1. 打开 ChatBox 页面
      2. 检查下拉框状态
    Expected Result: 显示错误状态或默认选项，不阻塞 UI
    Evidence: .sisyphus/evidence/task-5-error-state.png
  ```

  **Commit**: YES
  - Message: `feat(chat): add model selector to ChatInputBar`
  - Files: `autoflow-fe/src/components/Chat/ChatInputBar.vue`

---

- [ ] 6. **前端 - chatSSE 支持 modelId**

  **What to do**:
  - 修改 `src/api/chat.ts` 中的 `chatSSE()` 函数
  - 在发送请求时包含当前 Topic 的 `modelId`
  - 确保 modelId 为空时不发送该字段 (让后端使用默认)

  **Must NOT do**:
  - 不要改变 SSE 响应的解析逻辑
  - 不要添加 modelId 到 Message 类型 (后端处理)

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`frontend`, `api`, `sse`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 7, 8)
  - **Blocks**: None
  - **Blocked By**: Tasks 2, 3 (后端 chat modelId + 前端 Topic 类型)

  **References**:
  - `autoflow-fe/src/api/chat.ts:chatSSE()` - 目标函数
  - `autoflow-fe/src/stores/chat.ts` - 获取当前 Topic

  **Acceptance Criteria**:
  - [ ] chatSSE 发送请求时包含 modelId 字段
  - [ ] modelId 为空时不报错

  **QA Scenarios**:

  ```
  Scenario: 发送消息带 modelId
    Tool: Bash (curl or mock server)
    Preconditions: 有一个 modelId=xxx 的 Topic
    Steps:
      1. 模拟调用 chatSSE({input: "hello"})
      2. 检查发出的请求 body 包含 modelId
    Expected Result: 请求 body 为 {input: "hello", modelId: "xxx"}
    Evidence: .sisyphus/evidence/task-6-send-with-modelid.{ext}

  Scenario: 无 modelId 时发送
    Tool: Bash
    Preconditions: Topic 没有 modelId
    Steps:
      1. 模拟调用 chatSSE({input: "hello"})
      2. 检查请求不包含 modelId 或为 null
    Expected Result: 不报错，使用后端默认模型
    Evidence: .sisyphus/evidence/task-6-send-no-modelid.{ext}
  ```

  **Commit**: YES
  - Message: `feat(chat): pass modelId in chatSSE request`
  - Files: `autoflow-fe/src/api/chat.ts`

---

- [ ] 7. **前端 - Topic 创建时默认模型**

  **What to do**:
  - 在创建新 Topic 时，自动设置默认模型
  - 可选: 从 localStorage 读取上次使用的模型
  - 或使用模型列表的第一个模型作为默认

  **Must NOT do**:
  - 不要强制要求选择模型才能发送
  - 不要在后端不知道默认模型的情况下创建 Topic

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`frontend`, `vue`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 8)
  - **Blocks**: None
  - **Blocked By**: Task 3 (Topic 类型)

  **References**:
  - `autoflow-fe/src/stores/chat.ts` - Topic 创建逻辑
  - `autoflow-fe/src/api/model.ts` - 获取模型列表

  **Acceptance Criteria**:
  - [ ] 新建 Topic 时自动设置 modelId
  - [ ] 刷新页面后 Topic 的 modelId 保持

  **QA Scenarios**:

  ```
  Scenario: 新建 Topic 默认模型
    Tool: Playwright
    Preconditions: 已登录，模型列表可用
    Steps:
      1. 点击新建对话按钮
      2. 检查新 Topic 的 modelId 已设置
    Expected Result: 新 Topic 有默认 modelId
    Evidence: .sisyphus/evidence/task-7-new-topic-default.{ext}

  Scenario: 模型选择持久化
    Tool: Playwright
    Preconditions: 已有带 modelId 的 Topic
    Steps:
      1. 刷新页面
      2. 打开同一个 Topic
      3. 检查模型选择不变
    Expected Result: 模型选择被保留
    Evidence: .sisyphus/evidence/task-7-persistence.{ext}
  ```

  **Commit**: YES
  - Message: `feat(chat): set default model for new topics`
  - Files: `autoflow-fe/src/stores/chat.ts`

---

- [ ] 8. **前端 - vitest 测试**

  **What to do**:
  - 为 model 相关功能编写 vitest 测试
  - 测试覆盖:
    - `src/api/model.ts`: fetchModels 成功/失败
    - `src/stores/chat.ts`: setSelectedModelId action (如果添加)
    - Topic modelId 字段的持久化

  **Must NOT do**:
  - 不要添加端到端测试 (在 Task F1 中做)
  - 不要测试第三方库

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: [`testing`, `vitest`, `frontend`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 7)
  - **Blocks**: None
  - **Blocked By**: Tasks 4, 5, 6 (API + UI + chatSSE)

  **References**:
  - `autoflow-fe/src/api/chat.ts` - 现有测试参考
  - `autoflow-fe/src/stores/chat.ts` - store 测试位置
  - `autoflow-fe/vitest.config.ts` - vitest 配置

  **Acceptance Criteria**:
  - [ ] `bun test` 所有测试通过
  - [ ] 覆盖率报告生成

  **QA Scenarios**:

  ```
  Scenario: fetchModels 测试
    Tool: Bash
    Preconditions: vitest 配置正确
    Steps:
      1. bun test src/api/model.test.ts
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-8-model-api-tests.{ext}

  Scenario: chat store 测试
    Tool: Bash
    Preconditions: vitest 配置正确
    Steps:
      1. bun test src/stores/chat.test.ts
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-8-chat-store-tests.{ext}
  ```

  **Commit**: YES
  - Message: `test(chat): add vitest tests for model switching`
  - Files: `autoflow-fe/src/api/model.test.ts`, `autoflow-fe/src/stores/chat.test.ts`

---

## Final Verification Wave

- [ ] F1. **端到端集成验证** — `unspecified-high`

  Read the plan end-to-end. Execute EVERY QA scenario from EVERY task.
  Test the full flow: create topic -> select model -> send message -> verify modelId in request.
  Save to `.sisyphus/evidence/final-e2e/`.

  Output: `Flow [PASS/FAIL] | Evidence [N files]`

- [ ] F2. **计划合规审计** — `oracle`

  Verify each "Must Have" is implemented and each "Must NOT Have" is absent.
  Check evidence files exist in .sisyphus/evidence/.

  Output: `Must Have [N/N] | Must NOT Have [N/N] | VERDICT: APPROVE/REJECT`

---

## Commit Strategy

- **1**: `feat(backend): add Model entity and /api/models CRUD API` — backend files
- **2**: `feat(backend): support modelId parameter in chat SSE` — backend files
- **3**: `feat(chat): add modelId field to Topic type` — autoflow-fe/src/types/chat.ts
- **4**: `feat(api): add model.ts for model list API` — autoflow-fe/src/api/model.ts
- **5**: `feat(chat): add model selector to ChatInputBar` — autoflow-fe/src/components/Chat/ChatInputBar.vue
- **6**: `feat(chat): pass modelId in chatSSE request` — autoflow-fe/src/api/chat.ts
- **7**: `feat(chat): set default model for new topics` — autoflow-fe/src/stores/chat.ts
- **8**: `test(chat): add vitest tests for model switching` — autoflow-fe tests

---

## Success Criteria

### Verification Commands
```bash
# Backend
curl http://localhost:端口/api/models  # Expected: [{id, name}]

# Frontend build
cd autoflow-fe && bun run build  # Expected: success

# Frontend type check
cd autoflow-fe && npx tsc --noEmit  # Expected: no errors

# Frontend tests
cd autoflow-fe && bun test  # Expected: all pass
```

### Final Checklist
- [ ] `/api/models` 返回模型列表
- [ ] Chat SSE 支持 modelId 参数
- [ ] ChatInputBar 显示模型选择下拉框
- [ ] 选择模型后更新 Topic.modelId
- [ ] 新消息携带 modelId 发送到后端
- [ ] 新建 Topic 自动设置默认模型
- [ ] Topic 刷新后保持模型选择
- [ ] 所有 vitest 测试通过
- [ ] 构建成功无错误
