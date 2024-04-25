package io.autoflow.plugin.gemini;

import io.autoflow.spi.model.OptionValues;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/4/25
 */
@Data
public class GeminiParameter {
    @NotBlank
    private String projectId;
    @OptionValues({"us-central1", "us-east1", "europe-west4", "asia-east1"})
    @NotBlank
    private String location = "us-central1";
    @NotBlank
    @OptionValues({"gemini-pro", "gemini-pro-vision", "gemini-ultra", "gemini-ultra-vision"})
    private String model = "gemini-pro";
    private List<String> stopSequences;
    @DecimalMin("0")
    @DecimalMax("1")
    private Float temperature;
    @DecimalMin("0")
    @DecimalMax("1")
    private Float topP = 1f;
    @DecimalMin("0")
    @DecimalMax("1")
    private Float topK;
    private Integer candidateCount;
    private Integer maxOutputTokens;
    @NotBlank
    private String message;
}
