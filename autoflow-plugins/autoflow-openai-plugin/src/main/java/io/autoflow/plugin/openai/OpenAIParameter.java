package io.autoflow.plugin.openai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.ai.openai.api.ApiUtils;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/4/24
 */
@Data
public class OpenAIParameter {
    @NotBlank
    private String baseUrl = ApiUtils.DEFAULT_BASE_URL;
    @NotBlank
    private String openaiApiKey;
    @NotBlank
    private String model = "gpt-35-turbo";
    @NotEmpty
    private List<Message> messages;
}
