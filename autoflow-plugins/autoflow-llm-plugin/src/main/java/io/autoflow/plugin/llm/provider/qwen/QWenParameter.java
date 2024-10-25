package io.autoflow.plugin.llm.provider.qwen;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/10/25
 */
@Data
public class QWenParameter {
    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1";
    private String apiKey;
    @DecimalMin("-2.0")
    @DecimalMax("2.0")
    private Float repetitionPenalty;
    @DecimalMin("0")
    @DecimalMax("1")
    private Float temperature = 1.0f;
    @Min(0)
    @Max(1)
    private Integer topK;
    @DecimalMin("0")
    @DecimalMax("1")
    private Double topP;
    private Integer maxTokens = 8192;
    private List<String> stop;
    private Integer seed;
}
