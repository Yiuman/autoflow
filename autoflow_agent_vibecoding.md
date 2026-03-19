# 🧠 Autoflow Agent 内核 · VibeCoding 指南

## 🎯 一、目标（必须严格遵守）

实现一个 **Agent Runtime 内核**，具备：

1. ReAct 推理（LLM + Tool 调用）
2. 流式输出（token 级）
3. 多轮对话（Memory）
4. Tool = Node（复用 autoflow）
5. SPI 可扩展
6. 可插拔架构（非强依赖具体 LLM）

---

## ⚠️ 二、全局约束

1. 不允许使用 AiServices
2. 不允许写 demo 代码，必须是工程代码
3. 所有模块必须接口化
4. 必须支持多 session
5. 状态必须可持久化
6. Tool 必须走 registry
7. 必须支持 streaming

---

## 🏗️ 三、架构

agent/
 ├── engine/
 ├── context/
 ├── reasoner/
 ├── parser/
 ├── executor/
 ├── tool/
 ├── memory/
 └── spi/

---

## 🧩 四、核心接口

### AgentEngine
```java
public interface AgentEngine {
    void chat(String sessionId, String input, StreamListener listener);
}
```

### StreamListener
```java
public interface StreamListener {
    void onToken(String token);
    void onToolStart(String toolName);
    void onToolEnd(String toolName, Object result);
    void onComplete();
    void onError(Throwable e);
}
```

### AgentContext
```java
public class AgentContext {
    private String sessionId;
    private List<ChatMessage> messages;
    private Map<String, Object> variables;
}
```

### Reasoner
```java
public interface Reasoner {
    void think(AgentContext context, StreamListener listener);
}
```

### ActionParser
```java
public interface ActionParser {
    AgentAction parse(String content);
}
```

### NodeExecutor
```java
public interface NodeExecutor {
    Object execute(String nodeId, Map<String, Object> args);
}
```

### ToolRegistry
```java
public interface ToolRegistry {
    String getNodeId(String toolName);
}
```

### MemoryStore
```java
public interface MemoryStore {
    AgentContext load(String sessionId);
    void save(AgentContext context);
}
```

---

## ⚙️ 五、核心循环

1. 加载 context
2. 写入 user message
3. 循环：
   - LLM 推理（流式）
   - 解析 action
   - 如果无 tool → 结束
   - 有 tool → 执行 node
   - 写回结果
4. 保存 context

---

## 🧠 六、Reasoner

- 必须使用 Streaming 模型
- token 实时输出
- 完整结果写入 context

---

## 🔍 七、ActionParser

标准格式：

```json
{
  "action": "call_tool",
  "tool": "xxx",
  "args": {}
}
```

---

## 🔌 八、ToolRegistry

- toolName → nodeId
- 支持 SPI
- 支持动态注册

---

## 🧠 九、Memory

最小实现：

```java
ConcurrentHashMap<String, AgentContext>
```

推荐：

- 短期：内存
- 长期：ES

---

## 🚀 十、增强

- 最大 step 限制
- 超时控制
- 并行执行
- flow 调用

---

## 🧪 十一、验收

1. 普通对话
2. 单 tool
3. 多 tool
4. 多轮对话
5. 异常恢复

---

## 🧠 核心思想

这不是 Agent：

👉 这是一个 AI Runtime（可编排执行引擎）
