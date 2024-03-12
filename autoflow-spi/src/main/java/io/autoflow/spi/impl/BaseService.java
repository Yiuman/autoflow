package io.autoflow.spi.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.validation.ValidationUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.exception.InputValidateException;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import io.autoflow.spi.utils.PropertyUtils;
import jakarta.validation.ConstraintViolation;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @param <INPUT> 输入类型
 * @author yiuman
 * @date 2023/7/27
 */
public abstract class BaseService<INPUT> implements Service {

    @SuppressWarnings("unchecked")
    private final transient Class<INPUT> iputeDataClass = (Class<INPUT>) TypeUtil.getTypeArgument(getClass(), 0);
    private List<Property> properties;
    private String description;

    @Override
    public List<Property> getProperties() {
        if (Objects.isNull(properties)) {
            try {
                String propertiesJsonFile = StrUtil.format("{}.json", getClass().getName());
                String propertiesJsonStr = ResourceUtil.readUtf8Str(propertiesJsonFile);
                if (StrUtil.isNotBlank(propertiesJsonStr)) {
                    properties = JSONUtil.toList(propertiesJsonStr, Property.class);

                }
            } catch (cn.hutool.core.io.resource.NoResourceException ignore) {
            }
        }

        if (Objects.isNull(properties)) {
            properties = PropertyUtils.buildProperty(iputeDataClass);
        }
        return properties;
    }

    @Override
    public String getDescription() {
        if (Objects.isNull(description)) {
            try {
                description = ResourceUtil.readUtf8Str(StrUtil.format("{}.md", getClass().getName()));
            } catch (cn.hutool.core.io.resource.NoResourceException ignore) {
            }

        }
        return description;
    }

    @Override
    public ExecutionData execute(ExecutionContext executionContext) {
        INPUT input = buildInputData(executionContext);
        Set<ConstraintViolation<INPUT>> validated = ValidationUtil.validate(input);
        Assert.isTrue(CollUtil.isEmpty(validated), () -> new InputValidateException(validated));
        return execute(input);
    }

    protected INPUT buildInputData(ExecutionContext executionContext) {
        INPUT input = ReflectUtil.newInstanceIfPossible(iputeDataClass);
        BeanUtil.fillBean(input, new ExecutionContextValueProvider(executionContext), CopyOptions.create());
        return input;
    }

    public abstract ExecutionData execute(INPUT input);
}
