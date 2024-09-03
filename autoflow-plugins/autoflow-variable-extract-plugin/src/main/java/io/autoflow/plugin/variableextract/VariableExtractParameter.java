package io.autoflow.plugin.variableextract;

import io.autoflow.common.utils.NamedValue;
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
