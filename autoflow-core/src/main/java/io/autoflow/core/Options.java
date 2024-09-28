package io.autoflow.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.spi.OptionValueProvider;
import io.autoflow.spi.model.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author yiuman
 * @date 2024/9/27
 */
public final class Options {
    private static final List<OptionValueProvider> OPTION_VALUE_PROVIDERS = new ArrayList<>();

    static {
        ServiceLoader<OptionValueProvider> load = ServiceLoader.load(OptionValueProvider.class);
        load.forEach(OPTION_VALUE_PROVIDERS::add);
    }

    public static OptionValueProvider getOptionValueProvider(String id) {
        return CollUtil.findOne(OPTION_VALUE_PROVIDERS, optionValueProvider -> StrUtil.equals(id, optionValueProvider.getId()));
    }

    public static List<Option> getOptions(String id) {
        OptionValueProvider optionValueProvider = getOptionValueProvider(id);
        return optionValueProvider.getOptions();
    }
}
