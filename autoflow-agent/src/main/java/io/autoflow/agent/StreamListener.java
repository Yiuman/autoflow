package io.autoflow.agent.spi;

/**
 * Callback interface for streaming events from AgentEngine.
 */
public interface StreamListener {

    void onToken(String token);

    void onToolStart(String toolName);

    void onToolEnd(String toolName, Object result);

    void onComplete();

    void onError(Throwable e);
}
