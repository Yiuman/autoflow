package io.autoflow.plugin.llm.provider.gemini;

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
    private Double temperature = 1.0d;
    private Integer topK = 64;
    private Double topP = 0.95d;
    private Integer maxOutputTokens = 8192;
    private Integer candidateCount = 1;
    private List<String> stopSequences;
}
