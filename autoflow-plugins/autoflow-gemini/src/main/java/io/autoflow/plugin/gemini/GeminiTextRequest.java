package io.autoflow.plugin.gemini;

import io.autoflow.spi.enums.MessageType;
import io.autoflow.spi.model.ChatMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yiuman
 * @date 2024/4/26
 */
@Data
public class GeminiTextRequest {
    private final List<GeminiMessage> contents = new ArrayList<>();

    public GeminiTextRequest(List<ChatMessage> messages) {
        List<GeminiMessage> userMessages = messages.stream()
                .map(message -> new GeminiMessage(
                        MessageType.ASSISTANT == message.getType()
                                ? "ai"
                                : (MessageType.SYSTEM == message.getType() ? "model" : "user"),
                        message.getContent()
                ))
                .toList();
        contents.addAll(userMessages);
    }

}
