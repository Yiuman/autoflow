package io.autoflow.plugin.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/10/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiMessage {
    private String role;
    private List<GeminiPart> parts;

    public GeminiMessage(String role, String text) {
        this.role = role;
        this.parts = List.of(new GeminiPart(text));
    }
}
