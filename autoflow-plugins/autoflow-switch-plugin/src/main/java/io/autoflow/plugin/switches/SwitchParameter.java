package io.autoflow.plugin.switches;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/3/18
 */
@Data
public class SwitchParameter {
    @NotBlank
    private String express;
}
