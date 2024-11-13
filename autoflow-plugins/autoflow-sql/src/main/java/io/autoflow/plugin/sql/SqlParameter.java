package io.autoflow.plugin.sql;

import io.autoflow.spi.annotation.Code;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/3/1
 */
@Data
public class SqlParameter {
    @NotBlank
    @Code(lang = "sql")
    private String sql;
    @NotBlank
    private String url;
    private String username;
    private String password;
}
