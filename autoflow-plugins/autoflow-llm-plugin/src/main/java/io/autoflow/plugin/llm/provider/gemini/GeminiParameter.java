package io.autoflow.plugin.llm.provider.gemini;

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
public class GeminiParameter {
    @NotBlank
    private String apiKey;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double temperature = 1.0d;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double topP = 0.95d;
    private Integer topK = 64;
    private Integer maxOutputTokens = 8192;
    private Integer candidateCount = 1;
    private List<String> stopSequences;
}
