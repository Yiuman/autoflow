package io.autoflow.plugin.llm.provider.ollama;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/10/26
 */
@Data
public class OllamaParameter {
    private String modelName;
    private String baseUrl;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double temperature;
    private Integer topK;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double topP;
    private Double repeatPenalty;
    @DecimalMin("0")
    @DecimalMax("1")
    private Integer seed;
    private Integer numPredict;
    private Integer numCtx;
    private List<String> stop;
    private String format;
}
