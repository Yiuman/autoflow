package io.autoflow.spi.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import io.autoflow.spi.utils.PropertyUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author yiuman
 * @date 2023/7/27
 */
public abstract class BaseService<INPUT> implements Service {

    @SuppressWarnings("unchecked")
    private final Class<INPUT> iputeDataClass = (Class<INPUT>) TypeUtil.getTypeArgument(getClass(), 0);
    private List<Property> properties;

    @Override
    public List<Property> getProperties() {
        if (Objects.isNull(properties)) {
            properties = PropertyUtils.buildProperty(iputeDataClass);
        }

        return properties;
    }

    @Override
    public List<ExecutionData> execute(ExecutionContext executionContext) {
        return execute(buildInputData(executionContext));
    }

    protected INPUT buildInputData(ExecutionContext executionContext) {
        INPUT input = ReflectUtil.newInstanceIfPossible(iputeDataClass);
        BeanUtil.fillBean(input, new ExecutionContextValueProvider(executionContext), CopyOptions.create());
        return input;
    }

    public abstract List<ExecutionData> execute(INPUT input);
}
