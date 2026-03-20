package io.autoflow.app.model.sse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSSEEvent {

    private String type;

    private String content;

    private String toolName;

    private String arguments;

    private Object result;
}
