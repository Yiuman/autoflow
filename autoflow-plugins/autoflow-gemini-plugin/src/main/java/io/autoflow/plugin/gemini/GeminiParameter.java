package io.autoflow.plugin.gemini;

import io.autoflow.spi.model.ChatMessage;
import io.autoflow.spi.model.OptionValues;
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
    private String baseUrl = "https://generativelanguage.googleapis.com";
    @NotBlank
    private String apiKey;
    @NotBlank
    @OptionValues({"gemini-pro", "gemini-pro-vision", "gemini-ultra", "gemini-ultra-vision"})
    private String model = "gemini-pro";
    @NotEmpty
    private List<ChatMessage> messages = List.of(new ChatMessage());
}
