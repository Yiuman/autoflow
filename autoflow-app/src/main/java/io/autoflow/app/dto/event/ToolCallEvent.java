package io.autoflow.app.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event representing a tool call during chat.
 * Used to notify SSE clients when tools are executed.
 *
 * @author autoflow
 * @date 2025/03/04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolCallEvent extends ChatEvent {
    /**
     * Event type constant for tool call events
     */
    public static final String TYPE_TOOL_CALL = "tool_call";

    /**
     * Name of the tool being called
     */
    private String toolName;

    /**
     * Parameters passed to the tool (JSON format)
     */
    private String parameters;

    /**
     * Result returned by the tool (JSON format)
     */
    private String result;

    /**
     * Status of the tool call (pending/success/error)
     */
    private String status;

    /**
     * Tool call ID for tracking
     */
    private String toolCallId;

    /**
     * Constructs a new ToolCallEvent.
     *
     * @param sessionId the session ID
     * @param toolName  the name of the tool
     */
    public ToolCallEvent(final String sessionId, final String toolName) {
        super(TYPE_TOOL_CALL, sessionId);
        this.toolName = toolName;
        this.status = "pending";
    }

    /**
     * Constructs a new ToolCallEvent with full details.
     *
     * @param sessionId the session ID
     * @param toolName  the name of the tool
     * @param parameters the tool parameters
     * @param result    the tool result
     * @param status    the status of the call
     */
    public ToolCallEvent(final String sessionId, final String toolName,
                         final String parameters, final String result, final String status) {
        this(sessionId, toolName);
        this.parameters = parameters;
        this.result = result;
        this.status = status;
    }
}
