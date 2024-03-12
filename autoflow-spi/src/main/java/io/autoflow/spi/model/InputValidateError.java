package io.autoflow.spi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiuman
 * @date 2024/3/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputValidateError {
    private String parameter;
    private String message;
}
