package io.autoflow.plugin.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiuman
 * @date 2024/9/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmResult {
    private String text;

    public static LlmResult from(String text) {
        return new LlmResult(text);
    }
}
