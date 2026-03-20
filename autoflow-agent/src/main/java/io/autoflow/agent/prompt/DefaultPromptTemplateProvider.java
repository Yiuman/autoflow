package io.autoflow.agent.prompt;

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
