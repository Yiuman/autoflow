package io.autoflow.app.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Base class for chat-related events.
 * All chat events (message, tool call, error) extend this class.
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Data
public abstract class ChatEvent {
    /**
     * Event type (e.g., "message", "tool_call", "error")
     */
    private String type;

    /**
     * Session ID associated with this event
     */
    private String sessionId;

    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;

    /**
     * Constructs a new ChatEvent with the specified type and session ID.
     *
     * @param type      the event type
     * @param sessionId the session ID
     */
    protected ChatEvent(final String type, final String sessionId) {
        this.type = type;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }
}
