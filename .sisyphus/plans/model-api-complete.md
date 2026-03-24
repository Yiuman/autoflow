# Plan: 模型配置接口完善 (Model Configuration API)

## TL;DR

> **Quick Summary**: 为 `/api/models` 实现完整 CRUD 功能，包括后端数据库表/Service层和前端模型管理界面
> 
> **Deliverables**:
> - 后端: `af_model` 表 + ModelService + ModelController 完整实现
> - 前端: Model API 完整 CRUD + 模型管理列表页 + 模型配置表单页
> 
> **Estimated Effort**: Medium-Large
> **Parallel Execution**: YES - 后端和前端可并行开发
> **Critical Path**: 后端 schema → 后端 Service → 前端 API → 前端 UI

---

## Context

### Original Request
用户需要完善 `/api/models` 接口，实现**完整 CRUD**。用户明确选择"完整 CRUD"方案。

### Interview Summary
**Key Discussions**:
- 模型配置: 后端存储 JSON string，前端用 map 动态配置参数
- 参考现有: FormRenderer + Property 类型用于动态表单
- 范围: 完整管理界面 (列表 + 创建 + 编辑 + 删除)

**Research Findings**:
- Tech Stack: Spring Boot + MyBatis-Flex + ola-crud (后端), Vue 3 + TypeScript + Arco Design Vue (前端)
- 现有 Model 实体: id, name, baseUrl, apiKey, config
- 现有 FormRenderer 支持 Property[] 动态表单渲染
- **CRITICAL**: `af_model` 表在 schema.sql 中缺失

### Metis Review
**Identified Gaps** (addressed):
- schema.sql 缺失 af_model 表 → 将添加
- ModelController 空壳 → 将添加 @Query 和 Service 注入
- 无 ModelService → 将创建
- 前端 model.ts 只有 {id, name} → 将扩展完整字段

---

## Work Objectives

### Core Objective
实现模型配置的完整管理功能：后端 CRUD API + 前端管理界面

### Concrete Deliverables
- [ ] 后端: `af_model` 表定义添加到 schema.sql
- [ ] 后端: ModelService 接口和实现
- [ ] 后端: ModelQuery 查询条件类
- [ ] 后端: ModelController 完整实现
- [ ] 前端: model.ts 扩展类型 + CRUD API
- [ ] 前端: ModelList.vue 模型列表页
- [ ] 前端: ModelForm.vue 模型配置表单页
- [ ] 前端: 路由配置

### Definition of Done
- [ ] `curl /api/models` 返回模型列表
- [ ] `curl /api/models/{id}` 返回单个模型
- [ ] `POST /api/models` 创建模型
- [ ] `PUT /api/models/{id}` 更新模型
- [ ] `DELETE /api/models/{id}` 删除模型
- [ ] 前端模型列表页正常显示
- [ ] 前端可以创建/编辑/删除模型
- [ ] config JSON 可以通过 FormRenderer 动态配置

### Must Have
- 完整的 CRUD 操作
- 数据库持久化
- 前端动态配置表单

### Must NOT Have (Guardrails)
- 不实现模型调用/测试功能
- 不修改现有的 chat 接口
- 不添加模型权限控制
- 不添加批量操作
- 不添加模型版本历史

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: YES (JUnit 后端, vitest 前端)
- **Automated tests**: YES (tests-after)
- **Framework**: JUnit 5 (后端), vitest (前端)

### QA Policy
Every task MUST include agent-executed QA scenarios. Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — 数据库 + 后端基础):
├── Task 1: 后端 - 添加 af_model 表到 schema.sql
├── Task 2: 后端 - 创建 ModelService 接口和实现
├── Task 3: 后端 - 创建 ModelQuery 查询类
└── Task 4: 后端 - 更新 ModelController (注入 Service + @Query)

Wave 2 (After Wave 1 — 前端 API):
├── Task 5: 前端 - 扩展 Model 接口 + CRUD API 方法
└── Task 6: 前端 - 添加 API 错误处理和类型

Wave 3 (After Wave 2 — 前端 UI):
├── Task 7: 前端 - 创建 ModelList.vue 列表页
├── Task 8: 前端 - 创建 ModelForm.vue 表单页
└── Task 9: 前端 - 添加路由配置

Wave FINAL (After ALL tasks):
├── Task F1: 端到端集成验证
└── Task F2: 计划合规审计
```

### Dependency Matrix

| Task | Depends On | Blocks |
|------|------------|--------|
| 1 (schema) | — | 2, 4 |
| 2 (ModelService) | 1 | 4 |
| 3 (ModelQuery) | — | 4 |
| 4 (Controller) | 1, 2, 3 | — |
| 5 (Frontend API) | 4 | 7, 8 |
| 6 (API error handling) | 5 | 7, 8 |
| 7 (ModelList) | 5, 6 | 9 |
| 8 (ModelForm) | 5, 6 | 9 |
| 9 (Routes) | 7, 8 | — |
| F1 (E2E) | 4, 5, 6, 7, 8, 9 | — |

### Agent Dispatch Summary

- **Backend Agents**: Tasks 1-4 → `unspecified-high`
- **Frontend API**: Tasks 5-6 → `quick`
- **Frontend UI**: Tasks 7-9 → `visual-engineering`
- **Verification**: F1-F2 → `unspecified-high`

---

## TODOs

- [ ] 1. **后端 - 添加 af_model 表到 schema.sql**

  **What to do**:
  - 在 `autoflow-app/src/main/resources/sql/schema.sql` 中添加 af_model 表定义
  - 表结构参考 Model.java 实体: id (UUID), name, baseUrl, apiKey, config
  - 遵循现有表的风格 (表名、字段命名、注释)

  **Must NOT do**:
  - 不要修改其他已存在的表
  - 不要添加索引除非必要

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: [`backend`, `sql`, `mybatis-flex`]
  - Reason: 数据库迁移需要遵循现有模式

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Task 2, 4
  - **Blocked By**: None

  **References**:
  - `autoflow-app/src/main/resources/sql/schema.sql` - 现有表结构参考
  - `autoflow-app/src/main/java/io/autoflow/app/model/Model.java` - 实体字段定义

  **Acceptance Criteria**:
  - [ ] schema.sql 包含 af_model 表定义
  - [ ] 表字段与 Model.java 实体匹配

  **QA Scenarios**:

  ```
  Scenario: 验证 schema.sql 包含 af_model 表
    Tool: Bash
    Preconditions: schema.sql 文件存在
    Steps:
      1. grep -A 20 "af_model" schema.sql
    Expected Result: 找到 CREATE TABLE af_model 语句
    Evidence: .sisyphus/evidence/task-1-schema-check.{ext}
  ```

  **Commit**: YES
  - Message: `feat(schema): add af_model table definition`
  - Files: `autoflow-app/src/main/resources/sql/schema.sql`

---

- [ ] 2. **后端 - 创建 ModelService 接口和实现**

  **What to do**:
  - 创建 `ModelService.java` 接口，继承 `CrudService<Model>`
  - 创建 `ModelServiceImpl.java` 实现类，继承 `BaseService<Model>`
  - 遵循 TagService 模式

  **Must NOT do**:
  - 不要添加业务逻辑，只提供 CRUD
  - 不要添加特殊查询方法

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: [`backend`, `spring-boot`]
  - Reason: 需要遵循 ola-crud 框架模式

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Task 4
  - **Blocked By**: Task 1

  **References**:
  - `autoflow-app/src/main/java/io/autoflow/app/service/TagService.java` - Service 接口模式
  - `autoflow-app/src/main/java/io/autoflow/app/service/impl/ServiceEntityServiceImpl.java` - Service 实现模式
  - `autoflow-app/src/main/java/io/autoflow/app/model/Model.java` - 实体

  **Acceptance Criteria**:
  - [ ] ModelService.java 存在且继承 CrudService
  - [ ] ModelServiceImpl.java 存在且继承 BaseService
  - [ ] @Service 注解已添加

  **QA Scenarios**:

  ```
  Scenario: 编译检查
    Tool: Bash
    Preconditions: mvn 项目
    Steps:
      1. cd autoflow-app && mvn compile -q
    Expected Result: 编译成功，无错误
    Evidence: .sisyphus/evidence/task-2-compile.{ext}
  ```

  **Commit**: YES
  - Message: `feat(service): add ModelService interface and implementation`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/service/ModelService.java`, `autoflow-app/src/main/java/io/autoflow/app/service/impl/ModelServiceImpl.java`

---

- [ ] 3. **后端 - 创建 ModelQuery 查询类**

  **What to do**:
  - 创建 `ModelQuery.java` 查询条件类
  - 参考 TagQuery 模式，使用 @In, @Like 等注解
  - 支持按 name 模糊查询

  **Must NOT do**:
  - 不要添加复杂的查询条件

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`backend`]
  - Reason: 简单类，遵循现有模式

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Task 4
  - **Blocked By**: None

  **References**:
  - `autoflow-app/src/main/java/io/autoflow/app/query/TagQuery.java` - Query 类模式

  **Acceptance Criteria**:
  - [ ] ModelQuery.java 存在
  - [ ] @Data 注解已添加
  - [ ] 支持 name 模糊查询

  **QA Scenarios**:

  ```
  Scenario: 类型检查
    Tool: Bash
    Preconditions: Java 源文件存在
    Steps:
      1. cd autoflow-app && mvn compile -q
    Expected Result: 编译成功
    Evidence: .sisyphus/evidence/task-3-compile.{ext}
  ```

  **Commit**: YES
  - Message: `feat(query): add ModelQuery for filtering`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/query/ModelQuery.java`

---

- [ ] 4. **后端 - 更新 ModelController**

  **What to do**:
  - 更新 `ModelController.java`
  - 添加 `@Query(ModelQuery.class)` 注解
  - 注入 `ModelService`
  - 保持 @RequestMapping("/api/models")

  **Must NOT do**:
  - 不要改变路由路径
  - 不要添加自定义端点

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: [`backend`, `spring-boot`]
  - Reason: 需要遵循 REST 模式

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: After Wave 1
  - **Blocks**: 前端 API
  - **Blocked By**: Tasks 1, 2, 3

  **References**:
  - `autoflow-app/src/main/java/io/autoflow/app/rest/TagController.java` - Controller 模式
  - `autoflow-app/src/main/java/io/autoflow/app/rest/WorkflowController.java` - 有自定义 buildWrapper 的模式

  **Acceptance Criteria**:
  - [ ] @Query(ModelQuery.class) 注解存在
  - [ ] ModelService 已注入
  - [ ] 编译通过

  **QA Scenarios**:

  ```
  Scenario: API 列表查询
    Tool: Bash (curl)
    Preconditions: 后端服务运行中
    Steps:
      1. curl http://localhost:端口/api/models
    Expected Result: 返回模型列表 (JSON 数组)
    Evidence: .sisyphus/evidence/task-4-api-list.{ext}

  Scenario: API 单个查询
    Tool: Bash (curl)
    Preconditions: 后端服务运行中，有一个已知 model id
    Steps:
      1. curl http://localhost:端口/api/models/{id}
    Expected Result: 返回单个模型对象
    Evidence: .sisyphus/evidence/task-4-api-get.{ext}

  Scenario: API 创建
    Tool: Bash (curl)
    Preconditions: 后端服务运行中
    Steps:
      1. curl -X POST http://localhost:端口/api/models -H "Content-Type: application/json" -d '{"name":"test-model"}'
    Expected Result: 返回创建的模型对象 (含 id)
    Evidence: .sisyphus/evidence/task-4-api-create.{ext}

  Scenario: API 更新
    Tool: Bash (curl)
    Preconditions: 后端服务运行中，有一个已知 model id
    Steps:
      1. curl -X PUT http://localhost:端口/api/models/{id} -H "Content-Type: application/json" -d '{"name":"updated-name"}'
    Expected Result: 返回更新后的模型对象
    Evidence: .sisyphus/evidence/task-4-api-update.{ext}

  Scenario: API 删除
    Tool: Bash (curl)
    Preconditions: 后端服务运行中，有一个已知 model id
    Steps:
      1. curl -X DELETE http://localhost:端口/api/models/{id}
    Expected Result: 返回成功状态
    Evidence: .sisyphus/evidence/task-4-api-delete.{ext}
  ```

  **Commit**: YES
  - Message: `feat(controller): update ModelController with @Query and ModelService`
  - Files: `autoflow-app/src/main/java/io/autoflow/app/rest/ModelController.java`

---

- [ ] 5. **前端 - 扩展 Model 接口 + CRUD API 方法**

  **What to do**:
  - 扩展 `model.ts` 中的 Model 接口，添加 baseUrl, apiKey, config 字段
  - 添加 getModel, createModel, updateModel, deleteModel 方法
  - 使用现有 request.ts 工具
  - 添加类型定义

  **Must NOT do**:
  - 不要添加 UI 逻辑
  - 不要添加缓存

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`frontend`, `typescript`]
  - Reason: API 层代码

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2
  - **Blocks**: Tasks 7, 8
  - **Blocked By**: Task 4

  **References**:
  - `autoflow-fe/src/api/model.ts` - 当前文件
  - `autoflow-fe/src/api/crud.ts` - CRUD 请求生成器
  - `autoflow-fe/src/api/service.ts` - Service API 模式

  **Acceptance Criteria**:
  - [ ] Model 接口包含 id, name, baseUrl, apiKey, config
  - [ ] getModel(id) 方法存在
  - [ ] createModel(data) 方法存在
  - [ ] updateModel(id, data) 方法存在
  - [ ] deleteModel(id) 方法存在
  - [ ] TypeScript 编译通过

  **QA Scenarios**:

  ```
  Scenario: 类型检查
    Tool: Bash
    Preconditions: TypeScript 配置正确
    Steps:
      1. cd autoflow-fe && npx tsc --noEmit
    Expected Result: 无类型错误
    Evidence: .sisyphus/evidence/task-5-type-check.{ext}

  Scenario: API 方法存在
    Tool: Bash
    Preconditions: 源文件存在
    Steps:
      1. grep -E "getModel|createModel|updateModel|deleteModel" src/api/model.ts
    Expected Result: 找到所有 4 个方法定义
    Evidence: .sisyphus/evidence/task-5-methods.{ext}
  ```

  **Commit**: YES
  - Message: `feat(api): extend Model interface and add CRUD methods`
  - Files: `autoflow-fe/src/api/model.ts`

---

- [ ] 6. **前端 - 添加 API 错误处理和类型**

  **What to do**:
  - 添加错误类型定义
  - 确保 API 方法正确处理错误
  - 添加 loading 状态类型

  **Must NOT do**:
  - 不要改变现有 request.ts 的行为

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`frontend`]
  - Reason: 类型增强

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 5)
  - **Blocks**: Tasks 7, 8
  - **Blocked By**: Task 5

  **References**:
  - `autoflow-fe/src/api/service.ts` - 错误处理模式

  **Acceptance Criteria**:
  - [ ] 错误类型已定义
  - [ ] API 方法正确传播错误

  **QA Scenarios**:

  ```
  Scenario: 错误处理
    Tool: Bash
    Preconditions: 后端不可用或返回错误
    Steps:
      1. 模拟调用 getModel('invalid-id')
    Expected Result: 错误被正确抛出
    Evidence: .sisyphus/evidence/task-6-error-handling.{ext}
  ```

  **Commit**: YES
  - Message: `feat(api): add error handling to Model API`
  - Files: `autoflow-fe/src/api/model.ts`

---

- [ ] 7. **前端 - 创建 ModelList.vue 列表页**

  **What to do**:
  - 创建 `ModelList.vue` 组件
  - 使用 Arco Design Table 展示模型列表
  - 支持创建、编辑、删除操作
  - 包含加载状态和空状态

  **Must NOT do**:
  - 不要直接在列表页编辑配置 (使用 ModelForm)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend`, `vue`, `arco-design`]
  - Reason: UI 组件开发

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 9)
  - **Blocks**: None
  - **Blocked By**: Tasks 5, 6

  **References**:
  - `autoflow-fe/src/views/` - 查看现有页面结构
  - `autoflow-fe/src/components/FormRenderer/FormRenderer.vue` - 表单渲染参考
  - `autoflow-fe/src/api/model.ts` - API 调用

  **Acceptance Criteria**:
  - [ ] 表格显示模型列表
  - [ ] 创建按钮存在
  - [ ] 编辑按钮存在
  - [ ] 删除按钮存在
  - [ ] 删除有确认对话框
  - [ ] 加载状态显示
  - [ ] 空状态显示

  **QA Scenarios**:

  ```
  Scenario: 模型列表渲染
    Tool: Playwright
    Preconditions: 前端服务运行中，/models 路由可访问
    Steps:
      1. 打开 /models 页面
      2. 等待 .arco-table 或 .model-list 出现
    Expected Result: 表格正常渲染
    Evidence: .sisyphus/evidence/task-7-list-render.png

  Scenario: 创建模型按钮
    Tool: Playwright
    Preconditions: 模型列表页
    Steps:
      1. 查找 .btn-create 或类似创建按钮
      2. 点击创建按钮
    Expected Result: 弹出 ModelForm 或导航到创建页
    Evidence: .sisyphus/evidence/task-7-create-action.png

  Scenario: 删除模型
    Tool: Playwright
    Preconditions: 模型列表页有至少一个模型
    Steps:
      1. 点击删除按钮
      2. 确认删除对话框
    Expected Result: 模型从列表中移除
    Evidence: .sisyphus/evidence/task-7-delete-action.png
  ```

  **Commit**: YES
  - Message: `feat(ui): add ModelList.vue component`
  - Files: `autoflow-fe/src/views/Model/ModelList.vue`

---

- [ ] 8. **前端 - 创建 ModelForm.vue 表单页**

  **What to do**:
  - 创建 `ModelForm.vue` 组件
  - 基本字段: name, baseUrl, apiKey
  - config 字段使用 FormRenderer 动态渲染
  - 支持创建和编辑模式
  - 表单验证

  **Must NOT do**:
  - 不要实现模型调用功能

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend`, `vue`, `arco-design`, `typescript`]
  - Reason: 复杂表单组件

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 7, 9)
  - **Blocks**: None
  - **Blocked By**: Tasks 5, 6

  **References**:
  - `autoflow-fe/src/components/FormRenderer/FormRenderer.vue` - 动态表单渲染
  - `autoflow-fe/src/types/flow.ts:Property` - Property 类型定义
  - `autoflow-fe/src/api/model.ts` - API 调用

  **Acceptance Criteria**:
  - [ ] name 字段可输入
  - [ ] baseUrl 字段可输入
  - [ ] apiKey 字段可输入 (密码类型)
  - [ ] config 使用 FormRenderer 渲染
  - [ ] 保存按钮可用
  - [ ] 取消按钮可用
  - [ ] 表单验证工作正常

  **QA Scenarios**:

  ```
  Scenario: 表单渲染
    Tool: Playwright
    Preconditions: ModelForm 组件可访问
    Steps:
      1. 打开表单页
      2. 等待表单字段出现
    Expected Result: name, baseUrl, apiKey, config 字段都可见
    Evidence: .sisyphus/evidence/task-8-form-render.png

  Scenario: 表单验证
    Tool: Playwright
    Preconditions: ModelForm 在创建模式
    Steps:
      1. 不填写 name，直接点击保存
    Expected Result: 显示验证错误
    Evidence: .sisyphus/evidence/task-8-validation.png

  Scenario: FormRenderer 渲染 config
    Tool: Playwright
    Preconditions: config 有有效的 Property[] 数据
    Steps:
      1. 查看 config 区域
    Expected Result: FormRenderer 正确渲染动态字段
    Evidence: .sisyphus/evidence/task-8-config-render.png
  ```

  **Commit**: YES
  - Message: `feat(ui): add ModelForm.vue with FormRenderer`
  - Files: `autoflow-fe/src/views/Model/ModelForm.vue`

---

- [ ] 9. **前端 - 添加路由配置**

  **What to do**:
  - 在路由配置中添加 /models 路径
  - 添加 ModelList 和 ModelForm 路由
  - 可选: 添加子路由 (create, edit/:id)

  **Must NOT do**:
  - 不要改变现有路由

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`frontend`, `vue-router`]
  - Reason: 路由配置

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 7, 8)
  - **Blocks**: None
  - **Blocked By**: Tasks 7, 8

  **References**:
  - `autoflow-fe/src/router/` - 路由配置目录

  **Acceptance Criteria**:
  - [ ] /models 路由指向 ModelList
  - [ ] /models/create 路由指向 ModelForm (创建模式)
  - [ ] /models/edit/:id 路由指向 ModelForm (编辑模式)

  **QA Scenarios**:

  ```
  Scenario: 路由导航
    Tool: Playwright
    Preconditions: 前端服务运行中
    Steps:
      1. 导航到 /models
    Expected Result: 页面显示模型列表
    Evidence: .sisyphus/evidence/task-9-route-models.{ext}

  Scenario: 导航到创建页
    Tool: Playwright
    Preconditions: 路由正确配置
    Steps:
      1. 导航到 /models/create
    Expected Result: 显示创建表单
    Evidence: .sisyphus/evidence/task-9-route-create.{ext}
  ```

  **Commit**: YES
  - Message: `feat(router): add model management routes`
  - Files: `autoflow-fe/src/router/...`

---

## Final Verification Wave (MANDATORY)

> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.

- [ ] F1. **后端 CRUD 验证** — `unspecified-high`
  执行所有 CRUD API 测试: list, get, create, update, delete
  Output: `CRUD [PASS/FAIL] | Evidence [N files]`

- [ ] F2. **前端 UI 验证** — `unspecified-high` (+ `playwright` skill)
  测试模型列表页、创建表单、编辑表单
  Output: `UI [PASS/FAIL] | Evidence [N files]`

- [ ] F3. **集成验证** — `unspecified-high`
  后端 CRUD → 前端调用 → 数据一致性
  Output: `Integration [PASS/FAIL]`

- [ ] F4. **计划合规审计** — `oracle`
  验证每个 Must Have 已实现，每个 Must NOT Have 不存在
  Output: `Must Have [N/N] | Must NOT Have [N/N] | VERDICT`

---

## Commit Strategy

- **1**: `feat(schema): add af_model table definition` — schema.sql
- **2**: `feat(service): add ModelService interface and implementation` — ModelService.java, ModelServiceImpl.java
- **3**: `feat(query): add ModelQuery for filtering` — ModelQuery.java
- **4**: `feat(controller): update ModelController with @Query and ModelService` — ModelController.java
- **5**: `feat(api): extend Model interface and add CRUD methods` — model.ts
- **6**: `feat(api): add error handling to Model API` — model.ts
- **7**: `feat(ui): add ModelList.vue component` — ModelList.vue
- **8**: `feat(ui): add ModelForm.vue with FormRenderer` — ModelForm.vue
- **9**: `feat(router): add model management routes` — router files

---

## Success Criteria

### Verification Commands
```bash
# Backend
cd autoflow-app && mvn compile -q  # Expected: success
curl http://localhost:端口/api/models  # Expected: 模型列表 JSON

# Frontend
cd autoflow-fe && bun run build  # Expected: success
```

### Final Checklist
- [ ] af_model 表已创建
- [ ] ModelService 完整实现
- [ ] ModelController 支持 CRUD
- [ ] 前端 Model API 完整
- [ ] ModelList.vue 显示模型列表
- [ ] ModelForm.vue 可创建/编辑模型
- [ ] config 可用 FormRenderer 配置
- [ ] 路由正确配置
- [ ] 构建成功

---

## Commit Strategy

---

## Success Criteria

### Verification Commands
```bash
# Backend
curl http://localhost:端口/api/models  # Expected: 模型列表
curl http://localhost:端口/api/models/{id}  # Expected: 单个模型
curl -X POST http://localhost:端口/api/models -d '{"name":"test"}'  # Expected: 创建成功
curl -X PUT http://localhost:端口/api/models/{id} -d '{"name":"updated"}'  # Expected: 更新成功
curl -X DELETE http://localhost:端口/api/models/{id}  # Expected: 删除成功

# Frontend build
cd autoflow-fe && bun run build  # Expected: success
```

### Final Checklist
- [ ] /api/models CRUD 全部可用
- [ ] 前端模型列表页正常显示
- [ ] 前端可创建/编辑/删除模型
- [ ] config 可通过 FormRenderer 动态配置
- [ ] 构建成功无错误
