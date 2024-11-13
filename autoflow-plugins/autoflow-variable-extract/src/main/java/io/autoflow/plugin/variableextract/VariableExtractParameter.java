package io.autoflow.plugin.variableextract;

import io.autoflow.spi.model.NamedValue;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/9/3
 */
@Data
public class VariableExtractParameter {
    private List<NamedValue<Object>> attrs;
}
