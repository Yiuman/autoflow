package io.autoflow.spi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiuman
 * @date 2023/7/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Option {
    private String name;
    private Object value;
    private String description;
    private String displayTemplate;

    public Option(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}
