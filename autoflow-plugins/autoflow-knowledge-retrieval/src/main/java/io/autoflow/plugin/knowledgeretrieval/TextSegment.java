package io.autoflow.plugin.knowledgeretrieval;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/9/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextSegment {
    private String text;
    private Map<String, Object> metadata;
}
