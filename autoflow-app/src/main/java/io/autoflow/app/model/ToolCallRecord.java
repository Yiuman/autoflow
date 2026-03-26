package io.autoflow.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Tool call record for chat streaming events.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolCallRecord {
    private String toolId;
    private String toolName;
    private String arguments;
    private Object result;
}
