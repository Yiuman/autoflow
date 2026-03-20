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

    @NotBlank
    private String sessionId;

    @NotBlank
    @Size(max = 8192)
    private String input;
}
