package io.autoflow.app.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event representing a chat message.
 * Used for both user and assistant messages in the chat stream.
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageEvent extends ChatEvent {
    /**
     * Event type constant for message events
     */
    public static final String TYPE_MESSAGE = "message";

    /**
     * Role of the message sender (user/assistant/system)
     */
    private String role;

    /**
     * Content of the message
     */
    private String content;

    /**
     * Message ID (optional, for reference)
     */
    private String messageId;

    /**
     * Constructs a new MessageEvent.
     *
     * @param sessionId the session ID
     * @param role      the role of the message sender
     * @param content   the message content
     */
    public MessageEvent(final String sessionId, final String role, final String content) {
        super(TYPE_MESSAGE, sessionId);
        this.role = role;
        this.content = content;
    }

    /**
     * Constructs a new MessageEvent with message ID.
     *
     * @param sessionId the session ID
     * @param role      the role of the message sender
     * @param content   the message content
     * @param messageId the message ID
     */
    public MessageEvent(final String sessionId, final String role,
                        final String content, final String messageId) {
        this(sessionId, role, content);
        this.messageId = messageId;
    }
}
