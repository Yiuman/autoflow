package io.autoflow.plugin.gemini;

import io.autoflow.spi.model.OptionValues;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/4/25
 */
@Data
public class GeminiParameter {
    @NotBlank
    private String baseUrl = "https://generativelanguage.googleapis.com";
    @NotBlank
    private String apiKey;
    @NotBlank
    @OptionValues({"gemini-pro", "gemini-pro-vision", "gemini-ultra", "gemini-ultra-vision"})
    private String model = "gemini-pro";
    @NotBlank
    private String message;
}
