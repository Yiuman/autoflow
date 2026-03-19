package io.autoflow.agent;

/**
 * Callback interface for streaming events from AgentEngine.
 */
public interface StreamListener {

    void onToken(String token);

    void onToolStart(String toolName);

    void onToolEnd(String toolName, Object result);

    void onComplete();

    void onComplete(String fullOutput);

    void onToolCallComplete(String toolName, String arguments);

    void onError(Throwable e);
}
