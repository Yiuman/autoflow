package io.autoflow.agent;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.autoflow.spi.enums.MessageType;
import io.autoflow.spi.model.ChatMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime state for a single ReAct loop execution.
 * Not persisted across requests - the caller manages history.
 */
@Data
public class AgentContext {

    private String systemPrompt;
    private List<ChatMessage> messages = new ArrayList<>();
    private int stepCount = 0;
    private List<ToolSpecification> toolSpecifications;

    public static AgentContext from(ChatRequest request) {
        AgentContext context = new AgentContext();
        if (request.getHistory() != null) {
            context.messages.addAll(request.getHistory());
        }
        context.addUserMessage(request.getInput());
        context.systemPrompt = request.getSystemPrompt();
        return context;
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

    public void incrementStep() {
        stepCount++;
    }
}
