package io.autoflow.plugin.function;

import io.autoflow.spi.annotation.Select;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2025/11/14
 */
@Data
public class FunctionInput {
    @Select(provider = FunctionOptionProvider.class)
    private String function;
    private List<Object> args;
}
