package io.autoflow.plugin.openai;

import io.autoflow.spi.annotation.Select;
import io.autoflow.spi.model.ChatMessage;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
    @Select(options = {"gpt-3.5-turbo", "gpt-3.5-turbo-16k", "gpt-4"})
    private String model = "gpt-3.5-turbo";
    @DecimalMin("-2.0")
    @DecimalMax("2.0")
    private Float frequencyPenalty = 0f;
    private Integer maxTokens;
    private Integer n = 1;
    @DecimalMin("-2.0")
    @DecimalMax("2.0")
    private Float presencePenalty = 0f;
    private Integer seed;
    private List<String> stop;
    @DecimalMin("0")
    @DecimalMax("1")
    private Float temperature = 1f;
    @DecimalMin("0")
    @DecimalMax("1")
    private Float topP = 1f;
    private String user;
    @NotEmpty
    private List<ChatMessage> chatMessages = List.of(new ChatMessage());
}
