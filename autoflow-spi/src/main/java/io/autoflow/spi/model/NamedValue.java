package io.autoflow.spi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @param <T> 值类型
 * @author yiuman
 * @date 2024/4/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NamedValue<T> {
    private String name;
    private T value;
}
