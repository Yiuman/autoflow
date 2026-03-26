# Chat Route Dark Theme Adaptation

## TL;DR

> **Quick Summary**: Fix hardcoded `#fff` background colors in Chat components to use CSS variables for proper dark theme adaptation.
>
> **Deliverables**:
> - `ChatContainer.vue` - Replace hardcoded `#fff` with CSS variables
> - `ChatSidebar.vue` - Replace hardcoded `#fff` with CSS variables
> - `AttachmentPreview.vue` - Replace hardcoded icon color with CSS variable
> - `ChatMessageGroup.vue` - Clean up unnecessary fallback
>
> **Estimated Effort**: Small (< 1 hour)
> **Parallel Execution**: YES - 4 independent fixes
> **Critical Path**: None - all tasks independent

---

## Context

### Original Request
用户反馈 `/chat` 路由在黑暗主题下样式显示不正常，未经适配黑暗模式。

### Issue Analysis
Chat 组件中多处使用硬编码的 `#fff` 背景色，导致切换到黑暗主题时仍然显示白色背景，视觉上不协调。

项目使用 Arco Design Vue 的 CSS 变量系统，切换黑暗主题时只需要使用 `var(--color-*)` 变量即可自动适配。

### Root Cause
- `ChatContainer.vue` 第 30、51 行：`background-color: #fff`
- `ChatSidebar.vue` 第 90 行：`background-color: #fff`
- `AttachmentPreview.vue` 第 100 行：`color: #37a5aa` (文件图标颜色)

---

## Work Objectives

### Core Objective
修复 Chat 路由下所有组件的黑暗主题适配问题，将硬编码颜色替换为 CSS 变量。

### Concrete Deliverables
1. `autoflow-fe/src/components/Chat/layout/ChatContainer.vue` - 修复容器背景色
2. `autoflow-fe/src/components/Chat/layout/ChatSidebar.vue` - 修复侧边栏背景色
3. `autoflow-fe/src/components/Chat/components/AttachmentPreview.vue` - 修复图标颜色
4. `autoflow-fe/src/components/Chat/layout/ChatMessageGroup.vue` - 清理不必要的 fallback

### Definition of Done
- [ ] 所有硬编码 `#fff` 替换为 CSS 变量
- [ ] `var(--color-bg-2)` 用于容器背景
- [ ] 黑暗主题下显示正常

---

## Verification Strategy

### QA Policy
**Agent-Executed QA Only** - 每个 task 完成后执行验证。

**验证方式**: 手动检查 + 截图对比
- 切换到黑暗主题
- 访问 `/chat` 路由
- 验证背景色已正确适配

---

## Execution Strategy

### Parallel Execution Waves

所有 4 个任务相互独立，可并行执行：

```
Wave 1 (并行 - 4 个组件修复):
├── Task 1: ChatContainer.vue - 修复容器背景
├── Task 2: ChatSidebar.vue - 修复侧边栏背景
├── Task 3: AttachmentPreview.vue - 修复图标颜色
└── Task 4: ChatMessageGroup.vue - 清理 fallback
```

---

## TODOs

- [x] 1. ChatContainer.vue - 修复容器背景色

  **What to do**:
  - 将 `ChatContainer.vue` 第 30 行 `background-color: #fff;` 改为 `background-color: var(--color-bg-2);`
  - 将第 51 行 `background-color: #fff;` 改为 `background-color: var(--color-bg-2);`

  **Must NOT do**:
  - 不要修改其他样式
  - 不要添加新的 class

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的 CSS 变量替换，单文件操作
  - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2, 3, 4)
  - **Blocks**: None
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/components/Chat/layout/ChatContainer.vue:30,51` - 需要修改的位置

  **Acceptance Criteria**:
  - [ ] 第 30 行：`background-color: var(--color-bg-2);`
  - [ ] 第 51 行：`background-color: var(--color-bg-2);`

  **QA Scenarios**:

  \`\`\`
  Scenario: ChatContainer 在黑暗主题下背景正确
    Tool: manual verification
    Preconditions: 开启黑暗主题，访问 /chat 路由
    Steps:
      1. 切换到黑暗主题
      2. 访问 http://localhost:5173/chat
      3. 观察 ChatContainer 容器背景
    Expected Result: 背景色为深色（非白色），与主题协调
    Failure Indicators: 背景仍为白色 #fff
    Evidence: .sisyphus/evidence/task-1-dark-theme.png
  \`\`\`

  **Commit**: YES
  - Message: `fix(chat): adapt dark theme for ChatContainer`
  - Files: `autoflow-fe/src/components/Chat/layout/ChatContainer.vue`

---

- [x] 2. ChatSidebar.vue - 修复侧边栏背景色

  **What to do**:
  - 将 `ChatSidebar.vue` 第 90 行 `background-color: #fff;` 改为 `background-color: var(--color-bg-2);`

  **Must NOT do**:
  - 不要修改其他样式

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的 CSS 变量替换，单文件操作

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1, 3, 4)
  - **Blocks**: None
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/components/Chat/layout/ChatSidebar.vue:90` - 需要修改的位置

  **Acceptance Criteria**:
  - [ ] 第 90 行：`background-color: var(--color-bg-2);`

  **QA Scenarios**:

  \`\`\`
  Scenario: ChatSidebar 在黑暗主题下背景正确
    Tool: manual verification
    Preconditions: 开启黑暗主题，访问 /chat 路由
    Steps:
      1. 切换到黑暗主题
      2. 访问 http://localhost:5173/chat
      3. 观察左侧 Sidebar 背景
    Expected Result: 侧边栏背景为深色，与主内容区一致
    Failure Indicators: 侧边栏背景为白色
    Evidence: .sisyphus/evidence/task-2-dark-theme.png
  \`\`\`

  **Commit**: YES
  - Message: `fix(chat): adapt dark theme for ChatSidebar`
  - Files: `autoflow-fe/src/components/Chat/layout/ChatSidebar.vue`

---

- [x] 3. AttachmentPreview.vue - 修复文件图标颜色

  **What to do**:
  - 将 `AttachmentPreview.vue` 第 100 行 `color: #37a5aa;` 改为 `color: var(--color-primary);`

  **Must NOT do**:
  - 不要修改其他样式

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的 CSS 变量替换，单文件操作

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1, 2, 4)
  - **Blocks**: None
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/components/Chat/components/AttachmentPreview.vue:100` - 需要修改的位置

  **Acceptance Criteria**:
  - [ ] 第 100 行：`color: var(--color-primary);`

  **QA Scenarios**:

  \`\`\`
  Scenario: AttachmentPreview 图标在黑暗主题下颜色正确
    Tool: manual verification
    Preconditions: 开启黑暗主题，访问 /chat 路由
    Steps:
      1. 开启黑暗主题
      2. 发送消息并附加文件
      3. 观察附件预览图标颜色
    Expected Result: 图标颜色为主题适配色，非固定青色
    Failure Indicators: 图标仍为固定青色 #37a5aa
    Evidence: .sisyphus/evidence/task-3-dark-theme.png
  \`\`\`

  **Commit**: YES
  - Message: `fix(chat): adapt dark theme for AttachmentPreview icon`
  - Files: `autoflow-fe/src/components/Chat/components/AttachmentPreview.vue`

---

- [x] 4. ChatMessageGroup.vue - 清理不必要的 fallback

  **What to do**:
  - 将 `ChatMessageGroup.vue` 第 133 行 `var(--color-text-3, #999)` 中的 fallback `#999` 移除，改为 `var(--color-text-3)`

  **Must NOT do**:
  - 不要修改其他样式

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的 CSS 变量清理

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1, 2, 3)
  - **Blocks**: None
  - **Blocked By**: None

  **References**:
  - `autoflow-fe/src/components/Chat/layout/ChatMessageGroup.vue:133` - 需要修改的位置

  **Acceptance Criteria**:
  - [ ] 第 133 行：`background-color: var(--color-text-3);`

  **QA Scenarios**:

  \`\`\`
  Scenario: ChatMessageGroup 动画点在黑暗主题下可见
    Tool: manual verification
    Preconditions: 开启黑暗主题，消息流中显示加载动画
    Steps:
      1. 开启黑暗主题
      2. 发送消息，等待 AI 回复
      3. 观察加载动画点
    Expected Result: 动画点颜色正确显示
    Failure Indicators: 动画点不可见或颜色异常
    Evidence: .sisyphus/evidence/task-4-dark-theme.png
  \`\`\`

  **Commit**: YES
  - Message: `fix(chat): clean up unnecessary color fallback`
  - Files: `autoflow-fe/src/components/Chat/layout/ChatMessageGroup.vue`

---

## Final Verification Wave

- [x] F1. **黑暗主题完整性验证** — `unspecified-high`
  读取所有修改的文件，验证颜色替换正确。启动开发服务器，切换黑暗主题，访问 /chat 路由，截图保存到 `.sisyphus/evidence/final-dark-theme-verify.png`。

---

## Commit Strategy

每个 task 单独提交，或合并为一个提交：
- Message: `fix(chat): adapt dark theme for chat route components`
- Files:
  - `autoflow-fe/src/components/Chat/layout/ChatContainer.vue`
  - `autoflow-fe/src/components/Chat/layout/ChatSidebar.vue`
  - `autoflow-fe/src/components/Chat/components/AttachmentPreview.vue`
  - `autoflow-fe/src/components/Chat/layout/ChatMessageGroup.vue`

---

## Success Criteria

### Verification Commands
```bash
# 开发服务器运行
cd autoflow-fe && npm run dev

# 验证步骤
1. 访问 http://localhost:5173
2. 开启黑暗主题（右上角主题切换）
3. 访问 http://localhost:5173/chat
4. 确认所有组件背景色正确适配
```

### Final Checklist
- [ ] ChatContainer 背景：深色背景，非白色
- [ ] ChatSidebar 背景：与容器背景一致
- [ ] 附件图标颜色：主题适配色
- [ ] 加载动画点：可见且颜色正确
