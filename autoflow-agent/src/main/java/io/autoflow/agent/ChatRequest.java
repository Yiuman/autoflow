package io.autoflow.agent;

import io.autoflow.spi.model.ChatMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the input for an agent chat invocation.
 * The caller is responsible for loading history from persistence.
 */
@Data
public class ChatRequest {

    private String input;
    private List<ChatMessage> history;
    private String systemPrompt;

    public ChatRequest() {
        this.history = new ArrayList<>();
    }

    public ChatRequest(String input, List<ChatMessage> history) {
        this.input = input;
        this.history = history != null ? history : new ArrayList<>();
    }

    public ChatRequest(String input, List<ChatMessage> history, String systemPrompt) {
        this.input = input;
        this.history = history != null ? history : new ArrayList<>();
        this.systemPrompt = systemPrompt;
    }
}
