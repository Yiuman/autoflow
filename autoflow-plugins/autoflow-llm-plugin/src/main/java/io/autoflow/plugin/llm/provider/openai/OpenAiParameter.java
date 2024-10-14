package io.autoflow.plugin.llm.provider.openai;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/9/27
 */
@Data
public class OpenAiParameter {
    @NotBlank
    private String baseUrl = "https://api.openai.com/v1";
    @NotBlank
    private String apiKey;
    @DecimalMin("-2.0")
    @DecimalMax("2.0")
    private Double frequencyPenalty;
    @DecimalMin("-2.0")
    @DecimalMax("2.0")
    private Double presencePenalty;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double temperature = 0.7d;
    @DecimalMin("0")
    @DecimalMax("1")
    private Integer seed;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double topP;
    private Integer maxTokens = 4096;
    private List<String> stop;

    private String user;
    private String responseFormat;
}
