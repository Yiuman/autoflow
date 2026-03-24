# Plan: Parse OpenAI Thinking Content in onPartialResponse

## Context

### Problem
- LangChain4j 的 `returnThinking(true)` 只支持 DeepSeek 的 `reasoning_content` 字段
- OpenAI o1-mini 使用 content block 格式发送 thinking: `{"type":"thinking","thinking":"..."}`
- `onPartialThinking` 永远不会被调用，因为字段名和格式不匹配

### Goal
在 `onPartialResponse` 中解析 OpenAI 的 content block 格式，提取 thinking 内容并调用 `listener.onThinking()`

---

## Work Objectives

### Core Objective
修改 `ReActAgent.java` 的 `onPartialResponse` 方法，使其能从 OpenAI 的 content block 格式中解析出 thinking 内容

### Concrete Deliverables
- 修改 `ReActAgent.java` 第170-174行的 `onPartialResponse` 方法
- 添加 JSON 解析逻辑提取 `{"type":"thinking","thinking":"..."}` 格式的内容
- 当检测到 thinking content 时，调用 `listener.onThinking()`

### Definition of Done
- [ ] 发起 `/api/chat` 请求时，thinking 内容能正确触发 `onPartialThinking` 回调
- [ ] `ChatStreamListener` 发送 SSE "thinking" 事件
- [ ] 前端 `ThinkingBlock.vue` 正确显示 thinking 内容

### Must Have
- 解析 JSON 格式的 content block: `{"type":"thinking","thinking":"..."}`
- 解析数组格式: `[{"type":"thinking","thinking":"..."},...]`
- 当 token 是普通文本时，不影响正常流程

### Must NOT Have
- 不修改 `StreamListener` 接口（保持向后兼容）
- 不破坏现有的 token 处理逻辑

---

## Technical Approach

### 实际观察到的格式 (用户日志)

```
event:thinking
data:{"type":"thinking","content":"用中文打招呼，我应该用中文回复..."}

event:token
data:{"type":"token","content":"用中文打招呼，我应该用中文回复..."}
```

**关键发现**:
- SSE 事件名是 `thinking` 和 `token`
- 但 data 里的 `type` 字段也是 `thinking` 和 `token`
- `content` 字段包含完整内容（包括 `</think>` 标签）

### 实际流程分析

1. **LangChain4j 的 `onPartialThinking` 确实被调用了** (因为 `returnThinking(true)` 生效)
2. `content` 字段里包含了 **完整的 thinking 文本，包括 `</think>` 标签**
3. 需要在 `onPartialResponse` 里进一步解析，提取干净的 thinking 内容

### 解析策略

```java
@Override
public void onPartialResponse(String text) {
    if (text == null || text.isBlank()) {
        return;
    }

    String thinking = null;
    String token = text;

    // 方案1: 处理 </> 标签格式
    // 输入: "<think>让我想想...</think>答案是21"
    // 输出: thinking="让我想想..." token="答案是21"
    if (text.contains("</think>")) {
        int start = text.indexOf("<think>");
        int end = text.indexOf("</think>");
        if (start >= 0 && end > start) {
            thinking = text.substring(start + "<think>".length(), end);
            token = text.substring(end + "</think>".length());
        }
    }

    // 发送 thinking 事件 (如果解析到)
    if (thinking != null && !thinking.isBlank()) {
        listener.onThinking(thinking);
    }

    // 发送 token 事件 (剩余文本)
    if (!token.isBlank()) {
        output.append(token);
        fullOutput.append(token);
        listener.onToken(token);
    }
}
```
   <think>让我想想...</think>答案是21
   ```

2. **JSON Content Block**:
   ```json
   [{"type":"thinking","thinking":"让我想想..."},{"type":"text","text":"答案是21"}]
   ```

### 解析策略 (综合方案)

需要同时支持两种格式：

```java
// 伪代码
public void onPartialResponse(String text) {
    if (text == null || text.isBlank()) {
        // 普通处理
        return;
    }

    String remainingText = text;
    List<String> thinkingContents = new ArrayList<>();

    // 方案1: 尝试解析 JSON Content Block 格式
    if (text.startsWith("[") || text.startsWith("{")) {
        try {
            // 解析 JSON，提取 type="thinking" 的内容
            // 剩余部分作为普通 token
        } catch (Exception e) {
            // 解析失败，尝试标签格式
        }
    }

    // 方案2: 标签格式 (<think>...</think> 或 <thinking>...</thinking>)
    String[] startTags = {"<think>", "<thinking>"};
    String[] endTags = {"</think>", "</thinking>"};

    for (String startTag : startTags) {
        for (String endTag : endTags) {
            // 提取标签内容
            // 发送到 listener.onThinking()
            // 从原文移除标签
        }
    }

    // 处理剩余的普通文本 token
    if (!remainingText.isBlank()) {
        listener.onToken(remainingText);
    }
}
```

### 依赖
- 使用已有的 `jackson.databind.ObjectMapper` (已在 ReActAgent.java 第4行导入)
- 无需新增依赖

---

## Implementation Steps

### Task 1: Modify onPartialResponse in ReActAgent.java

**File**: `autoflow-agent/src/main/java/io/autoflow/agent/ReActAgent.java`  
**Location**: 第170-174行

**What to do**:
1. 检测 text 是否包含 `</think>` 标签
2. 提取 `<think>` 和 `</think>` 之间的 thinking 内容
3. 调用 `listener.onThinking(thinking)` 发送 thinking 事件
4. 将剩余文本（去除标签后的内容）调用 `listener.onToken(token)`

**Test cases to cover**:
- `text = "<think>让我想想...</think>答案是21"` → thinking="让我想想...", token="答案是21"
- `text = "<think>只用中文回复...</think>你好！"` → thinking="只用中文回复...", token="你好！"
- `text = "普通文本"` → 无 thinking，token="普通文本"
- `text = null 或 empty` → 不处理

**注意**: 现有代码在调用 `onPartialThinking` 时可能也会触发 `onPartialResponse`，需要确保不重复发送。

---

## References

**Pattern References**:
- `ReActAgent.java:170-174` - Current onPartialResponse implementation to modify

**Type References**:
- `StreamListener.java:12` - `void onThinking(String thinking)` interface method
- `ChatStreamListener.java:23-27` - SSE "thinking" event sending logic

**External References**:
- OpenAI o1-mini streaming format: content blocks with type "thinking" and "text"
