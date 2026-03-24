package io.autoflow.app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent chat request DTO.
 *
 * @author yiuman
 * @date 2024/4/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatRequest {
    private String sessionId;
    @NotBlank
    @Size(max = 8192)
    private String input;
    
    /**
     * Optional model ID to use for this chat session.
     * If null, the default model will be used.
     */
    private String modelId;
}
