package io.autoflow.agent;

/**
 * Callback interface for streaming events from AgentEngine.
 * Provides complete bidirectional streaming: LLM thinking + output, tool execution lifecycle.
 */
public interface StreamListener {

    /**
     * Called when LLM is thinking (reasoning process streaming).
     */
    void onThinking(String thinking);

    /**
     * Called for each output token from LLM.
     */
    void onToken(String token);

    /**
     * Called when a tool execution starts.
     * @param toolName the name of the tool
     * @param arguments the tool arguments as JSON string
     */
    void onToolCallStart(String toolId, String toolName, String arguments);

    /**
     * Called when a tool execution completes.
     * @param toolId the unique id of the tool invocation
     * @param toolName the name of the tool
     * @param result the execution result
     */
    void onToolCallEnd(String toolId, String toolName, Object result);

    /**
     * Called when the entire chat session completes.
     * @param fullOutput the complete LLM output
     */
    void onComplete(String fullOutput);

    /**
     * Called when a ReAct round completes (after tool execution, before next round).
     * This allows saving intermediate ASSISTANT messages in multi-round conversations.
     */
    void onRoundComplete();

    /**
     * Called when an error occurs.
     * @param e the error
     */
    void onError(Throwable e);
}
