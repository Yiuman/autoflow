package io.autoflow.plugin.openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResult {
    private MessageType messageType;
    private Map<String, Object> properties;
    private String text;
}
