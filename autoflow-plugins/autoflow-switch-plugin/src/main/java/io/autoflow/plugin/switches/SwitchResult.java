package io.autoflow.plugin.switches;

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
public class SwitchResult {
    private Object expressValue;
    private Boolean result;
}
