package io.autoflow.agent.context;

import io.autoflow.agent.AgentContext;
import io.autoflow.spi.model.ChatMessage;

/**
 * Thread-safe implementation of AgentContext.
 * Extends the base AgentContext class and adds synchronized access to mutating methods.
 */
public class AgentContextImpl extends AgentContext {

    public AgentContextImpl(String sessionId) {
        super(sessionId);
    }

    @Override
    public synchronized void addUserMessage(String content) {
        super.addUserMessage(content);
    }

    @Override
    public synchronized void addAssistantMessage(String content) {
        super.addAssistantMessage(content);
    }

    @Override
    public synchronized ChatMessage getLastUserMessage() {
        return super.getLastUserMessage();
    }

    @Override
    public synchronized void incrementStep() {
        super.incrementStep();
    }

    @Override
    public synchronized int getStepCount() {
        return super.getStepCount();
    }
}
