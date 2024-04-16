package io.autoflow.plugin.ifplugin;

import io.autoflow.plugin.ifplugin.enums.CalcType;
import io.autoflow.plugin.ifplugin.enums.Clause;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/4/16
 */
@Data
public class Condition {
    private Object dataKey;
    private Object value;
    private List<Condition> children;
    private CalcType calcType;
    private Clause clause = Clause.AND;
}
