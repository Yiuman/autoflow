package io.autoflow.spi.model;

import lombok.Data;

/**
 * @author yiuman
 * @date 2023/7/11
 */
@Data
public class Option {
    private String name;
    private Object value;
    private String description;
    private String displayTemplate;
}
