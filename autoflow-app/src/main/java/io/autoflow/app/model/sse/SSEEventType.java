package io.autoflow.app.model.sse;

import lombok.Getter;

@Getter
public enum SSEEventType {
    THINKING("thinking"),
    TOKEN("token"),
    TOOL_START("tool_start"),
    TOOL_END("tool_end"),
    COMPLETE("complete"),
    ERROR("error");

    private final String value;

    SSEEventType(String value) {
        this.value = value;
    }

}
