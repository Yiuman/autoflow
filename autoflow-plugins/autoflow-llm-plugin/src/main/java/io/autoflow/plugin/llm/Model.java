package io.autoflow.plugin.llm;

import lombok.Data;

/**
 * @author yiuman
 * @date 2024/9/27
 */
@Data
public class Model {
    private String provider;
    private String modelName;
    private String implClazz;
}
