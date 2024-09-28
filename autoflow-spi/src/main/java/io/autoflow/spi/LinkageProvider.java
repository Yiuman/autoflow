package io.autoflow.spi;

import io.autoflow.spi.model.Property;

import java.util.List;

/**
 * @param <VALUE> 联动的值
 * @author yiuman
 * @date 2024/9/27
 */
public interface LinkageProvider<VALUE> {

    List<Property> getLinkageProperties(VALUE value);

    String getId();
}