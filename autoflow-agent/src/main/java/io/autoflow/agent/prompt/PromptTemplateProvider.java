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
