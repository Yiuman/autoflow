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
    private String baseUrl = "https://api.openai.com";
    @NotBlank
    private String apiKey;
    @DecimalMin("-2.0")
    @DecimalMax("2.0")
    private Double frequencyPenalty = 0d;
    private Integer maxTokens;
    @DecimalMin("-2.0")
    @DecimalMax("2.0")
    private Double presencePenalty = 0d;
    private Integer seed;
    private List<String> stop;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double temperature = 1d;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double topP = 1d;
    private String user;
    private String responseFormat;
}
