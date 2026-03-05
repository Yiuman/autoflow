# SSE流式处理与统一消息存储实现计划

## TL;DR

> **快速摘要**: 修复SSE流式处理，使用ChatLanguageModelProvider#createStream实现真正的token/tool call流式；统一消息存储，将ToolCall并入ChatMessage表

> **交付物**:
> - Controller调用真正的流式方法
> - ChatService流式处理逻辑修复
> - 新增type/metadata字段的ChatMessage实体
> - 数据库迁移脚本
> - 单元测试

> **预估工作量**: Medium
> **并行执行**: YES - 3 waves
> **关键路径**: ChatMessage修改 → ChatService修复 → Controller修改 → 测试

---

## 背景

### 原始需求
1. **SSE流式处理**: 使用 `io.autoflow.plugin.llm.provider.ChatLanguageModelProvider#createStream` 实现真正的模型流式处理，处理token和tool call，通过SSE传递
2. **统一消息存储**: 将ToolCall和Message统一存储为ChatMessage，通过type字段区分

### 访谈总结
**关键讨论**:
- Controller当前调用sendMessageAsync，需要改为sendMessageStreaming
- sendMessageStreaming方法存在实现问题，需要修复
- 统一消息存储策略：ChatMessage表新增type和metadata字段

**研究结果**:
- ChatLanguageModelProvider.createStream() 返回 StreamingChatModel (langchain4j)
- 现有事件类: TokenEvent, ToolCallEvent, MessageEvent, ErrorEvent
- 项目使用JUnit 5进行测试
- 当前ChatMessage表: id, sessionId, role, content, createdAt
- 当前ToolCall表: id, messageId, toolName, parameters, result, status, createdAt

---

## 工作目标

### 核心目标
1. 实现真正的SSE流式处理，token和tool call都能通过SSE正确传递
2. 统一消息存储，简化查询逻辑

### 具体交付物
- [x] 修改Controller调用真正的流式方法
- [x] 修复ChatService.sendMessageStreaming实现
- [x] 修改ChatMessage实体，新增type和metadata字段
- [x] 数据库迁移脚本
- [x] 单元测试

### 完成定义
- [ ] SSE端点能返回真正的流式token
- [ ] Tool call事件能通过SSE发送
- [ ] 消息统一存储到ChatMessage表
- [ ] 所有测试通过

### 必须有
- token流式处理
- tool call事件处理
- 数据库向后兼容

### 禁止事项
- 不修改现有非相关的API
- 不删除现有功能

---

## 验证策略

### 测试决策
- **基础设施存在**: YES
- **自动化测试**: Tests-after
- **框架**: JUnit 5 (项目已有)

### QA策略
- 每个任务必须包含Agent执行QA场景
- 前端/UI: 使用Playwright验证
- API: 使用curl验证

---

## 执行策略

### 并行执行Wave

```
Wave 1 (立即启动 - 基础设施):
├── 任务1: 修改ChatMessage实体，新增type和metadata字段
├── 任务2: 创建数据库迁移脚本
└── 任务3: 修改ToolCallService适配统一存储

Wave 2 (Wave 1后 - 核心逻辑):
├── 任务4: 修复ChatService.sendMessageStreaming实现
├── 任务5: 修改Controller调用流式方法
├── 任务6: 添加TokenEvent和ToolCallEvent发送逻辑
└── 任务7: 修改getMessages方法支持type过滤

Wave 3 (Wave 2后 - 测试):
├── 任务8: 添加ChatService单元测试
├── 任务9: 添加ChatController集成测试
└── 任务10: 手动验证SSE流式
```

### 关键路径
ChatMessage修改 → ChatService修复 → Controller修改 → 测试

---

## 待办事项

- [ ] 1. 修改ChatMessage实体，新增type和metadata字段

  **需要做的**:
  - 在ChatMessage.java中添加type字段(String)
  - 在ChatMessage.java中添加metadata字段(String, JSON格式)
  - type字段可选值: message, token, tool_call, tool_result
  - metadata存储toolName, parameters, result, status等额外信息

  **禁止做的**:
  - 不删除现有字段

  **推荐Agent配置**:
  - **Category**: `quick`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: YES
  - **并行组**: Wave 1 (任务1, 2, 3)
  - **阻塞**: 任务4, 5, 6
  - **被阻塞**: 无

  **参考**:
  - `autoflow-app/.../model/ChatMessage.java` - 现有实体结构
  - `autoflow-app/.../model/ToolCall.java` - 需要合并的字段

  **验收标准**:
  - [ ] ChatMessage.java包含type字段
  - [ ] ChatMessage.java包含metadata字段
  - [ ] 编译通过

  **QA场景**:
  ```
  场景: 验证ChatMessage实体编译
    工具: Bash
    步骤:
      1. cd /Users/ganyaowen/codespace/github/autoflow && mvn compile -pl autoflow-app -am -q
    预期结果: 编译成功，无错误
    失败指示: 编译失败
    证据: 编译输出
  ```

  **提交**: YES
  - 信息: `refactor(app): add type and metadata fields to ChatMessage`
  - 文件: `autoflow-app/.../model/ChatMessage.java`

- [ ] 2. 创建数据库迁移脚本

  **需要做的**:
  - 创建MyBatis-Flex表结构变更脚本
  - 新增type列到af_chat_message表
  - 新增metadata列到af_chat_message表

  **禁止做的**:
  - 不删除现有数据

  **推荐Agent配置**:
  - **Category**: `quick`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: YES
  - **并行组**: Wave 1
  - **阻塞**: 无
  - **被阻塞**: 任务4, 5

  **参考**:
  - `autoflow-app/.../model/ChatMessage.java` - 实体定义

  **验收标准**:
  - [ ] 迁移脚本创建完成

  **QA场景**:
  ```
  场景: 验证迁移脚本语法
    工具: Bash
    步骤:
      1. 检查迁移脚本语法正确性
    预期结果: 脚本格式正确
    证据: 脚本文件
  ```

  **提交**: YES

- [ ] 3. 修改ToolCallService适配统一存储

  **需要做的**:
  - 修改ToolCallService，保存时同时写ChatMessage(type=tool_call)
  - 提供统一的查询接口

  **禁止做的**:
  - 不破坏现有ToolCall查询

  **推荐Agent配置**:
  - **Category**: `unspecified-high`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: YES
  - **并行组**: Wave 1
  - **阻塞**: 无
  - **被阻塞**: 任务7

  **参考**:
  - `autoflow-app/.../service/ToolCallService.java` - 现有服务

  **验收标准**:
  - [ ] ToolCallService保存时同时写入ChatMessage

  **QA场景**:
  ```
  场景: 验证ToolCallService编译
    工具: Bash
    步骤:
      1. mvn compile -pl autoflow-app -am -q
    预期结果: 编译成功
    证据: 编译输出
  ```

  **提交**: YES

- [ ] 4. 修复ChatService.sendMessageStreaming实现

  **需要做的**:
  - 使用ChatLanguageModelProvider#createStream获取StreamingChatModel
  - 实现StreamingChatResponseHandler处理token
  - 实现tool call的流式处理
  - 发送TokenEvent到SSE
  - 发送ToolCallEvent到SSE

  **禁止做的**:
  - 不修改非相关的业务逻辑

  **推荐Agent配置**:
  - **Category**: `deep`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: NO
  - **并行组**: Wave 2
  - **阻塞**: 任务8, 9, 10
  - **被阻塞**: 任务1, 2, 3

  **参考**:
  - `autoflow-app/.../service/ChatService.java` - 需要修改的文件
  - `autoflow-plugins/autoflow-llm/.../provider/ChatLanguageModelProvider.java` - createStream接口
  - `autoflow-app/.../dto/event/TokenEvent.java` - token事件
  - `autoflow-app/.../dto/event/ToolCallEvent.java` - tool call事件

  **验收标准**:
  - [ ] sendMessageStreaming使用createStream
  - [ ] token通过SSE发送
  - [ ] tool call通过SSE发送

  **QA场景**:
  ```
  场景: 验证流式方法编译
    工具: Bash
    步骤:
      1. mvn compile -pl autoflow-app -am -q
    预期结果: 编译成功
    证据: 编译输出

  场景: 验证token事件发送
    工具: Bash
    步骤:
      1. 启动应用
      2. curl -X POST "http://localhost:8080/api/chat/sessions/{id}/messages" -H "Content-Type: application/json" -d '{"content":"hello"}' --no-buffer
    预期结果: 收到SSE流，包含token事件
    证据: SSE响应
  ```

  **提交**: YES

- [ ] 5. 修改Controller调用流式方法

  **需要做的**:
  - 将ChatController.sendMessage中的sendMessageAsync改为sendMessageStreaming

  **禁止做的**:
  - 不修改其他端点

  **推荐Agent配置**:
  - **Category**: `quick`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: YES
  - **并行组**: Wave 2 (任务4, 5, 6, 7)
  - **阻塞**: 任务10
  - **被阻塞**: 任务1, 2, 3

  **参考**:
  - `autoflow-app/.../rest/ChatController.java` - 需要修改的文件

  **验收标准**:
  - [ ] Controller调用sendMessageStreaming

  **QA场景**:
  ```
  场景: 验证Controller编译
    工具: Bash
    步骤:
      1. mvn compile -pl autoflow-app -am -q
    预期结果: 编译成功
    证据: 编译输出
  ```

  **提交**: YES

- [ ] 6. 添加TokenEvent和ToolCallEvent发送逻辑

  **需要做的**:
  - 在sendMessageStreaming中确保TokenEvent正确发送
  - 在sendMessageStreaming中确保ToolCallEvent正确发送

  **禁止做的**:
  - 不修改已有事件类

  **推荐Agent配置**:
  - **Category**: `unspecified-high`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: YES
  - **并行组**: Wave 2
  - **阻塞**: 任务10
  - **被阻塞**: 任务1, 2, 3

  **参考**:
  - `autoflow-app/.../service/ChatService.java` - 已修改

  **验收标准**:
  - [ ] TokenEvent在SSE中发送
  - [ ] ToolCallEvent在SSE中发送

  **QA场景**:
  ```
  场景: 验证事件发送
    工具: Bash
    步骤:
      1. 启动应用并发送请求
      2. 检查SSE响应包含TokenEvent和ToolCallEvent
    预期结果: 事件正确发送
    证据: SSE响应
  ```

  **提交**: YES (与任务4合并)

- [ ] 7. 修改getMessages方法支持type过滤

  **需要做的**:
  - 修改ChatService.getMessages支持type参数
  - 修改ChatMessageService支持查询

  **禁止做的**:
  - 不破坏现有查询

  **推荐Agent配置**:
  - **Category**: `quick`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: YES
  - **并行组**: Wave 2
  - **阻塞**: 无
  - **被阻塞**: 任务8

  **验收标准**:
  - [ ] getMessages支持type过滤

  **QA场景**:
  ```
  场景: 验证方法签名
    工具: Bash
    步骤:
      1. mvn compile -pl autoflow-app -am -q
    预期结果: 编译成功
    证据: 编译输出
  ```

  **提交**: YES

- [ ] 8. 添加ChatService单元测试

  **需要做的**:
  - 创建ChatServiceTest测试类
  - 测试sendMessageStreaming方法
  - 测试统一消息存储

  **禁止做的**:
  - 不添加需要外部依赖的集成测试

  **推荐Agent配置**:
  - **Category**: `unspecified-high`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: NO
  - **并行组**: Wave 3
  - **阻塞**: 无
  - **被阻塞**: 任务4, 5, 6, 7

  **参考**:
  - `autoflow-core/src/test/java/io/autoflow/core/ServicesTest.java` - 测试示例

  **验收标准**:
  - [ ] 测试类创建
  - [ ] 测试通过

  **QA场景**:
  ```
  场景: 运行单元测试
    工具: Bash
    步骤:
      1. mvn test -pl autoflow-app -Dtest=ChatServiceTest
    预期结果: 测试通过
    证据: 测试输出
  ```

  **提交**: YES

- [ ] 9. 添加ChatController集成测试

  **需要做的**:
  - 创建ChatControllerTest测试类
  - 测试SSE端点

  **禁止做的**:
  - 不测试真实LLM调用

  **推荐Agent配置**:
  - **Category**: `unspecified-high`
  - **Skills**: []

  **并行化**:
  - **可并行运行**: YES
  - **并行组**: Wave 3
  - **阻塞**: 无
  - **被阻塞**: 任务4, 5, 6, 7

  **验收标准**:
  - [ ] 测试类创建
  - [ ] 测试通过

  **QA场景**:
  ```
  场景: 运行集成测试
    工具: Bash
    步骤:
      1. mvn test -pl autoflow-app -Dtest=ChatControllerTest
    预期结果: 测试通过
    证据: 测试输出
  ```

  **提交**: YES

- [ ] 10. 手动验证SSE流式

  **需要做的**:
  - 启动应用
  - 发送聊天请求
  - 验证SSE流返回token
  - 验证tool call事件

  **禁止做的**:
  - 不在生产环境测试

  **推荐Agent配置**:
  - **Category**: `unspecified-high`
  - **Skills**: ["playwright"]

  **并行化**:
  - **可并行运行**: NO
  - **并行组**: Wave 3
  - **阻塞**: 无
  - **被阻塞**: 任务4, 5, 6

  **验收标准**:
  - [ ] SSE流正常工作
  - [ ] Token事件可见
  - [ ] ToolCall事件可见

  **QA场景**:
  ```
  场景: SSE流式验证
    工具: interactive_bash
    步骤:
      1. 启动应用: mvn spring-boot:run -pl autoflow-app
      2. 发送请求: curl -N "http://localhost:8080/api/chat/sessions/test/messages" -d '{"content":"hello"}'
    预期结果: 收到流式响应
    证据: 终端输出
  ```

  **提交**: NO

---

## 最终验证

### 验证命令
```bash
mvn test -pl autoflow-app
mvn compile -pl autoflow-app -am
```

### 最终检查清单
- [ ] 所有必须项存在
- [ ] 所有禁止项不存在
- [ ] 所有测试通过
