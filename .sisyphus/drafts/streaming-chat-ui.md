# Draft: Streaming Chat UI (Claude Style)

## Requirements (confirmed)

### 1. Scope & Purpose
- **Purpose**: Agent对话 - 调用LLM，流式输出，处理工具调用
- **Target**: Create a new streaming chat UI component for AI agent interactions
- **Backend**: 无需改动后端，仅参考后端事件枚举

### 2. UI Style
- **Style**: Claude风格 - 卡片式展示，清晰展示完整思考过程和工具调用
- **Layout**: 
  - 左侧可选：历史对话列表
  - 右侧/主区域：当前对话卡片
  - 底部：输入框

### 3. Think/思考过程展示
- **Display**: 显示为AI的"内心独白" (collapsible section)
- **Behavior**: 
  - 默认展开显示思考内容
  - 用户可折叠/展开
  - 视觉上与最终回复区分开

### 4. 工具调用展示
- **Display**: 折叠式 (Collapsible)
- **Behavior**:
  - 默认折叠，显示工具名称和状态
  - 点击展开查看：工具名、参数、结果
  - 支持多个工具调用链式展示

### 5. 后端事件参考 (StreamListener.java)
```java
onToken(String token)           // LLM流式输出
onToolStart(String toolName)    // 工具开始执行
onToolEnd(String toolName, Object result)  // 工具执行结束
onToolCallComplete(String toolName, String arguments)  // 工具调用完成
onComplete()                   // 对话完成
onComplete(String fullOutput)   // 完成并输出完整内容
onError(Throwable e)           // 错误
```

### 6. 前端技术栈
- Vue 3 + TypeScript (Composition API)
- Arco Design Vue
- @microsoft/fetch-event-source (已有)
- Pinia (状态管理已有)

## Technical Decisions

### 架构考虑
- 新建 `src/components/StreamingChat/` 目录
- 参考现有 `Chat.vue`, `Message.vue` 的样式
- 扩展现有 `types/chat.d.ts` 类型定义

### Mock方案
- 由于后端SSE暂不改动，前端需要：
  - 实现完整的流式UI组件
  - 提供Mock数据测试UI交互
  - 或对接现有的Flow SSE (已有点击流式示例)

## Open Questions
- [x] 所有需求已确认
