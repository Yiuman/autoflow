package io.autoflow.spi.model;

import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/11
 */
@Data
public class SimpleProperty implements Property {
    private String type;
    private String name;
    private String displayName;
    private String description;
    private Object defaultValue;
    private List<Option> options;
    private List<SimpleProperty> properties;
}
