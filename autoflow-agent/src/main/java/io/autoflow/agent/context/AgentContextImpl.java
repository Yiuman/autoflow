package io.autoflow.agent.context;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.autoflow.agent.AgentContext;
import io.autoflow.spi.model.ChatMessage;

import java.util.List;

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

    @Override
    public synchronized List<ToolSpecification> getToolSpecifications() {
        return super.getToolSpecifications();
    }

    @Override
    public synchronized void setToolSpecifications(List<ToolSpecification> toolSpecifications) {
        super.setToolSpecifications(toolSpecifications);
    }
}
