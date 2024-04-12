package io.autoflow.plugin.switches;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiuman
 * @date 2024/3/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwitchParameter {
    @NotBlank
    private String express;
}
