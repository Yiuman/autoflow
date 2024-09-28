package io.autoflow.spi;

import io.autoflow.spi.model.Option;

import java.util.List;

/**
 * 选项提供器
 *
 * @author yiuman
 * @date 2024/9/27
 */
public interface OptionValueProvider {

    List<Option> getOptions();

    String getId();
}