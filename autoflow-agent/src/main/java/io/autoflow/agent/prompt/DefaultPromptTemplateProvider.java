package io.autoflow.agent.prompt;

/**
 * 默认的 ReAct 提示词提供者.
 * 
 * <p>提供结构化的推理指导，包含:
 * - 显式 Thought 推理步骤
 * - 工具使用策略 ("仅在必要时使用")
 * - 自纠正机制 (工具失败后反思重试)
 * - Final Answer 终止条件
 */
public class DefaultPromptTemplateProvider implements PromptTemplateProvider {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        You are a helpful AI assistant with access to tools.

        ## Guidelines
        1. Think step-by-step before taking action - use Thought to reason through the problem
        2. Use tools only when necessary - if you know the answer, respond directly
        3. When a tool fails, acknowledge the error, reflect on what went wrong, and try alternative approaches
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

        ## Self-Correction
        If a tool fails or returns an unexpected result:

        1. **Reflect**: Analyze what went wrong:
           - Was the tool called with wrong arguments?
           - Is there a different tool that could achieve the same goal?
           - Is the task even possible with available tools?

        2. **Plan Fix**: Determine an alternative approach

        3. **Retry**: Call a different tool or same tool with corrected arguments

        Example of self-correction after tool failure:
        ```
        Thought: The calculator returned "Error: division by zero".\s
        Reflection: I tried to divide by zero. I need to check if the divisor is valid before dividing.
        Action: evaluate
        Action Input: {"expression": "if(b != 0, a / b, 'undefined')", "a": 10, "b": 0}
        ...
        ```

        ## Important
        - When you have completed the task, provide your Final Answer
        - Maximum 3 reflection attempts per failed tool call
       \s""";

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
}
