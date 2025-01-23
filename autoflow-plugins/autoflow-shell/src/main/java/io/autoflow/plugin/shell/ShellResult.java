package io.autoflow.plugin.shell;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiuman
 * @date 2025/1/22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShellResult {
    private Integer exitStatusCode = -1;
    private String output;
}
