# autoflow-agent ReAct Prompt 增强工作计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan.

**Goal:** 为 autoflow-agent 添加结构化的 ReAct 提示词模板，包含显式推理引导（Thought）、工具使用策略和 Final Answer 终止条件。

**Architecture:** 保持现有 LangChain4j function calling 架构不变，通过 `PromptTemplateProvider` 接口提供可配置的 system prompt，增强推理步骤指导。

**Tech Stack:** Java, LangChain4j, JUnit5

---

## Context

### 当前状态
- `DefaultAgentEngine` 使用 LangChain4j 原生 function calling
- Tools 通过 `ToolSpecification` API 传递，不在 prompt 中
- System prompt 由调用方外部传入，agent 模块无内置模板
- 缺少显式推理步骤（Thought）、错误处理指导、终止条件

### 用户需求
- 完善 system prompt 模板，添加推理指导
- 保持 LangChain4j 架构不变
- 不改变 tools 传递方式

---

## File Structure

```
autoflow-agent/src/main/java/io/autoflow/agent/
├── prompt/                              # [NEW] prompt 包
│   ├── PromptTemplateProvider.java       # [NEW] 提示词提供者接口
│   └── DefaultPromptTemplateProvider.java # [NEW] 默认实现
└── DefaultAgentEngine.java              # [MODIFY] 集成 PromptTemplateProvider

autoflow-agent/src/main/resources/
└── prompts/                            # [NEW] 提示词模板目录
    └── react-default.md                 # [NEW] 默认 ReAct 提示词模板
```

---

## Chunk 1: 基础设施 - PromptTemplateProvider 接口

### Task 1: 创建 PromptTemplateProvider 接口

**Files:**
- Create: `autoflow-agent/src/main/java/io/autoflow/agent/prompt/PromptTemplateProvider.java`

**What to do:**

```java
package io.autoflow.agent.prompt;

/**
 * 提供 Agent 使用的提示词模板.
 * 
 * <p>提示词包含推理指导、工具使用策略、输出格式规范，
 * 但不包含工具描述（工具通过 ToolSpecification API 传递）。
 */
public interface PromptTemplateProvider {

    /**
     * 获取系统提示词模板.
     * 
     * <p>模板可包含以下占位符:
     * - {max_steps} - 最大步数
     * - {step_count} - 当前步骤数
     * - {language} - 响应语言 (可选)
     *
     * @return 系统提示词模板
     */
    String getSystemPromptTemplate();

    /**
     * 获取用户消息的前缀提示.
     * 
     * <p>在用户消息之前添加，指导模型如何响应.
     *
     * @return 前缀提示，若无则返回空字符串
     */
    default String getUserMessagePrefix() {
        return "";
    }

    /**
     * 获取提示词名称 (用于日志和调试).
     *
     * @return 提示词名称
     */
    default String getName() {
        return "default";
    }
}
```

**QA Scenarios:**

```
Scenario: Interface contract verification
  Tool: javac
  Preconditions: 编译成功
  Steps:
    1. 创建接口实现类实现所有方法
    2. 验证接口方法可被调用
  Expected Result: 所有方法返回预期值
  Evidence: 编译无错误
```

---

### Task 2: 创建 DefaultPromptTemplateProvider

**Files:**
- Create: `autoflow-agent/src/main/java/io/autoflow/agent/prompt/DefaultPromptTemplateProvider.java`

**What to do:**

```java
package io.autoflow.agent.prompt;

import java.util.Map;

/**
 * 默认的 ReAct 提示词提供者.
 * 
 * <p>提供结构化的推理指导，包含:
 * - 显式 Thought 推理步骤
 * - 工具使用策略 ("仅在必要时使用")
 * - Final Answer 终止条件
 * - 步骤计数
 */
public class DefaultPromptTemplateProvider implements PromptTemplateProvider {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        You are a helpful AI assistant with access to tools.

        ## Guidelines
        1. Think step-by-step before taking action - use Thought to reason through the problem
        2. Use tools only when necessary - if you know the answer, respond directly
        3. When a tool fails, acknowledge the error and try alternative approaches
        4. Be concise but thorough in your reasoning

        ## Response Format
        When using tools, follow this format:

        Question: {user_question}
        Thought: [Describe your reasoning - what you know, what you need to find out, and your plan]
        Action: [Tool name from available tools, only if needed]
        Action Input: [Arguments in JSON format]
        Observation: [Result will appear here after tool execution]
        ... (Thought/Action/Observation can repeat as needed)

        Thought: Based on my reasoning and observations, I now have the answer.
        Final Answer: [Your concise response to the user]

        ## Important
        - When you have completed the task, provide your Final Answer
        - If you cannot complete the task after {max_steps} steps, provide whatever answer you have

        Current step: {step_count} of {max_steps}
        """;

    private final int maxSteps;

    public DefaultPromptTemplateProvider() {
        this(10);
    }

    public DefaultPromptTemplateProvider(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    @Override
    public String getSystemPromptTemplate() {
        return SYSTEM_PROMPT_TEMPLATE;
    }

    @Override
    public String getUserMessagePrefix() {
        return "Answer the following question:\n";
    }

    @Override
    public String getName() {
        return "react-default";
    }

    /**
     * 格式化系统提示词，填充占位符.
     *
     * @param stepCount 当前步骤 (1-based)
     * @return 格式化后的提示词
     */
    public String formatSystemPrompt(int stepCount) {
        return SYSTEM_PROMPT_TEMPLATE
            .replace("{max_steps}", String.valueOf(maxSteps))
            .replace("{step_count}", String.valueOf(stepCount));
    }
}
```

**QA Scenarios:**

```
Scenario: Template formatting with placeholders
  Tool: junit
  Preconditions: DefaultPromptTemplateProvider 实例化
  Steps:
    1. 调用 formatSystemPrompt(3)
    2. 验证输出包含 "Current step: 3 of 10"
  Expected Result: 占位符被正确替换
  Evidence: 测试通过

Scenario: Custom max steps
  Tool: junit
  Preconditions: maxSteps=5
  Steps:
    1. 调用 formatSystemPrompt(1)
    2. 验证输出包含 "Current step: 1 of 5"
  Expected Result: 自定义 maxSteps 生效
  Evidence: 测试通过
```

---

## Chunk 2: 集成 - 更新 DefaultAgentEngine

### Task 3: 添加 PromptTemplateProvider 支持

**Files:**
- Modify: `autoflow-agent/src/main/java/io/autoflow/agent/DefaultAgentEngine.java:36-56`

**What to do:**

在 `DefaultAgentEngine` 构造函数中添加 `PromptTemplateProvider` 参数:

```java
public class DefaultAgentEngine implements AgentEngine {

    private final MemoryStore memoryStore;
    private final StreamingChatModel streamingChatModel;
    private final NodeExecutor nodeExecutor;
    private final ToolRegistry toolRegistry;
    private final int maxSteps;
    private final PromptTemplateProvider promptTemplateProvider;  // [ADD]
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CompletableFuture<Object>> toolFutures = new ConcurrentHashMap<>();

    // [ADD] 新构造函数
    public DefaultAgentEngine(
            MemoryStore memoryStore,
            StreamingChatModel streamingChatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            PromptTemplateProvider promptTemplateProvider) {
        this(memoryStore, streamingChatModel, nodeExecutor, toolRegistry, 
             promptTemplateProvider, 10);
    }

    // [ADD] 新构造函数 with maxSteps
    public DefaultAgentEngine(
            MemoryStore memoryStore,
            StreamingChatModel streamingChatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            PromptTemplateProvider promptTemplateProvider,
            int maxSteps) {
        this.memoryStore = memoryStore;
        this.streamingChatModel = streamingChatModel;
        this.nodeExecutor = nodeExecutor;
        this.toolRegistry = toolRegistry;
        this.promptTemplateProvider = promptTemplateProvider;
        this.maxSteps = maxSteps;
        this.objectMapper = new ObjectMapper();
    }

    // [MODIFY] 保留原有构造函数，添加默认 provider
    public DefaultAgentEngine(
            MemoryStore memoryStore,
            StreamingChatModel streamingChatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry) {
        this(memoryStore, streamingChatModel, nodeExecutor, toolRegistry, 
             new DefaultPromptTemplateProvider(), 10);
    }

    public DefaultAgentEngine(
            MemoryStore memoryStore,
            StreamingChatModel streamingChatModel,
            NodeExecutor nodeExecutor,
            ToolRegistry toolRegistry,
            int maxSteps) {
        this(memoryStore, streamingChatModel, nodeExecutor, toolRegistry,
             new DefaultPromptTemplateProvider(maxSteps), maxSteps);
    }
    
    // ... rest of the class
}
```

**Must NOT do:**
- 不改变现有的 ReAct 循环逻辑
- 不改变 tools 的传递方式（保持 ToolSpecification API）

**QA Scenarios:**

```
Scenario: Default provider is used when not specified
  Tool: javac
  Preconditions: 使用三参数构造函数
  Steps:
    1. 实例化 engine
    2. 验证使用 DefaultPromptTemplateProvider
  Expected Result: 编译成功，默认 provider 被使用
  Evidence: 编译无错误

Scenario: Custom provider is used when specified
  Tool: javac
  Preconditions: 提供自定义 provider
  Steps:
    1. 实现自定义 PromptTemplateProvider
    2. 使用五参数构造函数
    3. 验证自定义 provider 被使用
  Expected Result: 编译成功，自定义 provider 被注入
  Evidence: 编译无错误
```

---

### Task 4: 更新 executeReactLoop 使用动态提示词

**Files:**
- Modify: `autoflow-agent/src/main/java/io/autoflow/agent/DefaultAgentEngine.java:85-100`

**What to do:**

```java
private void executeReactLoop(AgentContext context, StreamListener listener) {
    for (int i = 0; i < maxSteps; i++) {
        int currentStep = context.getStepCount();
        log.info("[Agent] Step {} started", currentStep);

        // [MODIFY] 设置格式化后的系统提示词（带步骤计数）
        if (promptTemplateProvider != null) {
            String formattedPrompt = ((DefaultPromptTemplateProvider) promptTemplateProvider)
                .formatSystemPrompt(currentStep);
            context.setSystemPrompt(formattedPrompt);
        }

        // Tools are executed asynchronously in callLlmWithStreaming
        LlmResult result = callLlmWithStreaming(context, listener);
        log.info("[Agent] LLM output: {}", result.text);

        // Only check if there are tool calls, don't execute them
        if (result.toolExecutionRequests == null || result.toolExecutionRequests.isEmpty()) {
            log.info("[Agent] No more tool calls, stopping loop");
            break;
        }
    }
}
```

**QA Scenarios:**

```
Scenario: Step count is passed to prompt
  Tool: junit
  Preconditions: ReActIntegrationTest
  Steps:
    1. 运行测试
    2. 验证日志中显示 "Step 1 started", "Step 2 started"
  Expected Result: 步骤计数正确传递
  Evidence: 测试通过
```

---

## Chunk 3: Few-shot 示例和模板文件

### Task 5: 创建提示词模板资源文件

**Files:**
- Create: `autoflow-agent/src/main/resources/prompts/react-default.md`

**What to do:**

```markdown
# ReAct Default Prompt Template

You are a helpful AI assistant with access to tools.

## Available Tools
(Tools are provided separately via the ToolSpecification API - do NOT include tool descriptions here)

## Guidelines

1. **Think step-by-step** - Use Thought to reason through the problem before taking action
2. **Use tools wisely** - Only use tools when necessary. If you know the answer from your knowledge, respond directly without calling tools
3. **Handle errors gracefully** - When a tool fails, acknowledge the error and try an alternative approach or explain the limitation
4. **Be concise** - Keep your thoughts and responses focused and concise

## Response Format

When you need to use tools, follow this format exactly:

```
Question: [The user's question]
Thought: [Describe your reasoning - what you know, what information you need, and your plan]
Action: [Tool name, only if needed]
Action Input: [Arguments in JSON format: {"arg1": "value1", "arg2": "value2"}]
Observation: [The result will appear here after tool execution]
... (Repeat Thought/Action/Observation as needed)

Thought: Based on my reasoning and the observations, I now have the answer.
Final Answer: [Your concise response to the user]
```

## Examples

### Example 1: Direct Answer (No Tool Needed)
```
Question: What is the capital of France?
Thought: This is a factual question about geography. I know that Paris is the capital of France, so I can answer directly without using any tools.
Final Answer: The capital of France is Paris.
```

### Example 2: Using a Tool
```
Question: What is the current weather in Tokyo?
Thought: The user is asking about current weather, which requires real-time data. I should use a weather tool to get the current conditions in Tokyo.
Action: get_weather
Action Input: {"location": "Tokyo", "unit": "celsius"}
Observation: {"temperature": 22, "condition": "Partly Cloudy", "humidity": 65}
Thought: The tool returned the current weather for Tokyo. I now have the answer.
Final Answer: Currently in Tokyo it's 22°C and partly cloudy with 65% humidity.
```

### Example 3: Multiple Tool Calls
```
Question: If I have 3 apples and I buy 5 more, then give away 2, how many do I have?
Thought: This is a simple arithmetic problem. Let me calculate: 3 + 5 = 8, then 8 - 2 = 6. I can answer this directly without tools.
Final Answer: You would have 6 apples.
```

## Important Notes

- When you have completed the user's request, provide your Final Answer
- The Final Answer should be concise and directly address the question
- Do not include the "Observation:" label unless you have actually called a tool
- Current step: {step_count} of {max_steps}
```

**QA Scenarios:**

```
Scenario: Template file exists and is readable
  Tool: bash
  Preconditions: 文件已创建
  Steps:
    1. 验证文件存在: ls autoflow-agent/src/main/resources/prompts/
    2. 验证内容非空
  Expected Result: 文件存在且可读
  Evidence: 命令输出确认文件存在
```

---

## Chunk 4: 测试更新

### Task 6: 更新 ReActIntegrationTest

**Files:**
- Modify: `autoflow-agent/src/test/java/io/autoflow/agent/ReActIntegrationTest.java:79-90`

**What to do:**

```java
@Test
void react_withRealModel_singleTurn() {
    String sessionId = "test-real-" + System.currentTimeMillis();
    
    // [MODIFY] 使用新的 PromptTemplateProvider
    PromptTemplateProvider promptProvider = new DefaultPromptTemplateProvider(5);
    
    AgentContext context = new AgentContext(sessionId);
    context.setSystemPrompt(promptProvider.formatSystemPrompt(1));
    memoryStore.save(context);

    // ... rest of test (same as before)
}
```

**QA Scenarios:**

```
Scenario: Test uses new prompt template
  Tool: mvn
  Preconditions: 修改后的测试
  Steps:
    1. 运行: mvn test -Dtest=ReActIntegrationTest
    2. 验证测试通过
  Expected Result: 测试通过，新的提示词被使用
  Evidence: BUILD SUCCESS
```

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — `oracle`
  验证所有 Task 都有实现，文件路径正确

- [ ] F2. **Code Quality Review** — `unspecified-high`
  运行 `mvn compile` 和 `mvn test` 确保无编译错误

- [ ] F3. **API Compatibility Check** — `quick`
  验证原有构造函数仍然工作（向后兼容）

- [ ] F4. **Integration Test** — `unspecified-high`
  运行 ReActIntegrationTest 验证功能正常

---

## Success Criteria

1. ✅ 新增 `PromptTemplateProvider` 接口
2. ✅ 新增 `DefaultPromptTemplateProvider` 默认实现
3. ✅ `DefaultAgentEngine` 支持注入自定义 prompt provider
4. ✅ 保持原有构造函数向后兼容
5. ✅ 提示词包含 Thought/Action/Final Answer 指导
6. ✅ 测试通过

---

## Commit Strategy

- **1**: `feat(agent): add PromptTemplateProvider interface and default implementation`
  Files: `prompt/PromptTemplateProvider.java`, `prompt/DefaultPromptTemplateProvider.java`, `resources/prompts/react-default.md`

- **2**: `feat(agent): integrate PromptTemplateProvider into DefaultAgentEngine`
  Files: `DefaultAgentEngine.java`

- **3**: `test(agent): update ReActIntegrationTest with new prompt provider`
  Files: `ReActIntegrationTest.java`
