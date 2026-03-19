package io.autoflow.agent;

/**
 * Parses LLM output string into structured AgentAction.
 *
 * @author yiuman
 * @date 2024/10/11
 */
public interface ActionParser {

    /**
     * Parses LLM output string into structured AgentAction.
     *
     * @param content The raw string output from LLM
     * @return Structured AgentAction parsed from content
     */
    AgentAction parse(String content);
}
