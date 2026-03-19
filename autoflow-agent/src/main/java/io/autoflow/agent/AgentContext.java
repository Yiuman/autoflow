package io.autoflow.agent;

import io.autoflow.spi.enums.MessageType;
import io.autoflow.spi.model.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent context holding conversation state for a session.
 */
public class AgentContext {

    private String sessionId;
    private String systemPrompt;
    private List<ChatMessage> messages = new ArrayList<>();
    private Map<String, Object> variables = new HashMap<>();
    private int stepCount = 0;

    public AgentContext(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void addUserMessage(String content) {
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.USER);
        message.setContent(content);
        messages.add(message);
    }

    public void addAssistantMessage(String content) {
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.ASSISTANT);
        message.setContent(content);
        messages.add(message);
    }

    public ChatMessage getLastUserMessage() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg.getType() == MessageType.USER) {
                return msg;
            }
        }
        return null;
    }

    public void incrementStep() {
        stepCount++;
    }
}
