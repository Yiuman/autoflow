package io.autoflow.plugin.ifplugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiuman
 * @date 2024/3/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IfResult {
    private String conditionStr;
    private Boolean result;
}
