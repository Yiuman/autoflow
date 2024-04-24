package io.autoflow.plugin.openai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/4/24
 */
@Data
public class OpenAIParameter {
    @NotBlank
    private String openaiApiKey;
    @NotBlank
    private String model;
}
