package io.autoflow.agent;

/**
 * Represents a tool call with its execution result.
 *
 * @param toolId the unique id of the tool invocation
 * @param toolName the name of the tool
 * @param arguments the tool arguments as JSON string
 * @param result the execution result
 */
public record ToolCall(String toolId, String toolName, String arguments, Object result) {
}
