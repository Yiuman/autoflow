package io.autoflow.app.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event representing an error during chat.
 * Used to notify SSE clients when errors occur.
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorEvent extends ChatEvent {
    /**
     * Event type constant for error events
     */
    public static final String TYPE_ERROR = "error";

    /**
     * Error code for categorization
     */
    private String errorCode;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Detailed error information (optional)
     */
    private String details;

    /**
     * Constructs a new ErrorEvent.
     *
     * @param sessionId the session ID
     * @param message   the error message
     */
    public ErrorEvent(final String sessionId, final String message) {
        super(TYPE_ERROR, sessionId);
        this.message = message;
    }

    /**
     * Constructs a new ErrorEvent with error code.
     *
     * @param sessionId the session ID
     * @param errorCode the error code
     * @param message   the error message
     */
    public ErrorEvent(final String sessionId, final String errorCode, final String message) {
        this(sessionId, message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new ErrorEvent with full details.
     *
     * @param sessionId the session ID
     * @param errorCode the error code
     * @param message   the error message
     * @param details   the detailed error information
     */
    public ErrorEvent(final String sessionId, final String errorCode,
                      final String message, final String details) {
        this(sessionId, errorCode, message);
        this.details = details;
    }
}
