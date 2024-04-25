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
    @NotBlank
    private String location;
    @NotBlank
    @OptionValues({"standard", "creative", "informative", "roleplay", "collaborative"})
    private String model = "standard";
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
    @NotEmpty
    private List<Message> messages;
}
