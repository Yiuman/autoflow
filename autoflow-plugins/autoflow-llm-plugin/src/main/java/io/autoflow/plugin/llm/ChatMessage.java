package io.autoflow.plugin.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/9/26
 */
@Data
public class ChatMessage {
    private MessageType type;
    private String content;

    public dev.langchain4j.data.message.ChatMessage toLangChainMessage() {
        return switch (type) {
            case USER -> UserMessage.from(content);
            case ASSISTANT -> AiMessage.from(content);
            default -> SystemMessage.from(content);
        };
    }
}
