# Design: Fix LangChainReasoner Tool Passing Bug

## Problem

**Bug**: `io.autoflow.agent.LangChainReasoner` 调用模型时没有传入 `tools`，导致外面的 Agent 永远无法调用工具。

**Root Cause**: 
- `LangChainReasoner.think()` 调用 `streamingChatModel.chat(messages, handler)` 时只传了 messages
- LangChain4j 需要通过 `ChatRequest` 并设置 `.toolSpecifications(...)` 才能让 LLM 知道有哪些工具可用

## Architecture

```
DefaultAgentEngine
├── ToolRegistry toolRegistry  ← 已有工具注册表
├── Reasoner reasoner         ← 需要接收 tools
└── AgentContext              ← 只有 messages，无 tools

LangChainReasoner
└── StreamingChatModel.chat(messages)  ← 缺少 toolSpecifications
```

## Solution

### 1. 新增 ToolSpecificationConverter

将 autoflow `Service` 转换为 LangChain4j `ToolSpecification`。

**文件**: `autoflow-agent/src/main/java/io/autoflow/agent/tool/ToolSpecificationConverter.java`

```java
public class ToolSpecificationConverter {
    // Service.name → ToolSpecification.name
    // Service.getProperties() → JSON Schema parameters
    // Property.getType() → JSON Schema type (string/number/boolean/object/array)
    // Property.getName() → parameter name
    // Property.getDescription() → parameter description
}
```

### 2. 扩展 Reasoner 接口

添加重载方法支持传入 tools。

**文件**: `autoflow-agent/src/main/java/io/autoflow/agent/Reasoner.java`

```java
public interface Reasoner {
    void think(AgentContext context, StreamListener listener);
    
    // 新增：支持传入 tool specifications
    default void think(AgentContext context, StreamListener listener, 
                       List<ToolSpecification> tools) {
        think(context, listener); // 默认实现兼容现有实现
    }
}
```

### 3. 修改 LangChainReasoner

使用 `ChatRequest` 传入 tools。

**文件**: `autoflow-agent/src/main/java/io/autoflow/agent/LangChainReasoner.java`

```java
@Override
public void think(AgentContext context, StreamListener listener, 
                  List<ToolSpecification> tools) {
    // 构建 ChatRequest
    ChatRequest request = ChatRequest.builder()
        .messages(convertMessages(context))
        .toolSpecifications(tools)  // ← 关键改动
        .build();
    
    streamingChatModel.chat(request, handler);
}
```

### 4. 修改 DefaultAgentEngine

从 ToolRegistry 获取工具并传给 Reasoner。

**文件**: `autoflow-agent/src/main/java/io/autoflow/agent/DefaultAgentEngine.java`

```java
// 需要新增字段存储 tools
private final ToolRegistry toolRegistry;
private List<ToolSpecification> toolSpecifications;  // 新增

// 在 callReasonerWithStreaming 时传入
private String callReasonerWithStreaming(AgentContext context, StreamListener listener) {
    reasoner.think(context, listener, toolSpecifications);  // 传入 tools
}
```

## ToolSpecification 转换规则

| autoflow Property | LangChain4j ToolSpecification |
|-------------------|-------------------------------|
| `getName()` | parameter name |
| `getDescription()` | parameter description |
| `getType()` → "string"/"number"/"boolean"/"object"/"array" | JSON Schema type |
| `getDefaultValue()` | default value |
| `getProperties()` (nested) | nested properties for object type |
| `getValidateRules()` | required array (if rules indicate required) |

## Files to Change

1. **NEW**: `autoflow-agent/src/main/java/io/autoflow/agent/tool/ToolSpecificationConverter.java`
2. **MODIFY**: `autoflow-agent/src/main/java/io/autoflow/agent/Reasoner.java` - add overloaded method
3. **MODIFY**: `autoflow-agent/src/main/java/io/autoflow/agent/LangChainReasoner.java` - use ChatRequest with tools
4. **MODIFY**: `autoflow-agent/src/main/java/io/autoflow/agent/DefaultAgentEngine.java` - pass tools to reasoner

## Test Strategy

- 修改 `ReActIntegrationTest` 验证工具调用流程
- 已有 `calculate` tool 注册，可以测试 `{"action":"call_tool","tool":"calculate","args":{"a":2,"b":3}}`
