package io.autoflow.plugin.gemini;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/26
 */
@Data
public class GeminiTextRequest {
    private final List<Map<String, Object>> contents = new ArrayList<>();

    public GeminiTextRequest(String message) {
        contents.add(Map.of("parts", List.of(Map.of("text", message))));
    }
}
