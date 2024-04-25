package io.autoflow.plugin.gemini;

import lombok.Data;
import org.springframework.ai.chat.messages.MessageType;

@Data
public class Message {
    private MessageType messageType = MessageType.USER;
    private String content;
}
