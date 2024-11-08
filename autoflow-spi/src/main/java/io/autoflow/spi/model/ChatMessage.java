package io.autoflow.spi.model;

import io.autoflow.spi.enums.MessageType;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/10/11
 */
@Data
public class ChatMessage {
    private MessageType type = MessageType.USER;
    private String content;
}
