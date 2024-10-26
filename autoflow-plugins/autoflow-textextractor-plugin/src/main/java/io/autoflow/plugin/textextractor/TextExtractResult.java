package io.autoflow.plugin.textextractor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiuman
 * @date 2024/10/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextExtractResult {
    private String text;
}
