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
import io.autoflow.spi.I18n;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.exception.InputValidateException;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import io.autoflow.spi.utils.PropertyUtils;
import jakarta.validation.ConstraintViolation;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @param <INPUT>  输入类型
 * @param <OUTPUT> 输出类型
 * @author yiuman
 * @date 2023/7/27
 */
@SuppressWarnings("unchecked")
public abstract class BaseService<INPUT, OUTPUT> implements Service<OUTPUT>, I18n {

    private final transient Class<INPUT> inputClass = (Class<INPUT>) TypeUtil.getTypeArgument(getClass(), 0);
    private final transient Class<OUTPUT> outputClass = (Class<OUTPUT>) TypeUtil.getClass(
            TypeUtil.getReturnType(
                    ReflectUtil.getMethod(getClass(), "execute", inputClass, ExecutionContext.class)
            )
    );
    private static final CopyOptions DEFAULT_COPY_OPTION = CopyOptions.create();
    private List<Property> properties;
    private List<Property> outputType;
    private String description;

    @Override
    public List<Property> getProperties() {
        if (Objects.isNull(properties)) {
            properties = PropertyUtils.buildProperties(getClass(), inputClass);
        }

        return properties;
    }

    @Override
    public List<Property> getOutputType() {
        if (Objects.isNull(outputType)) {
            outputType = PropertyUtils.buildProperties(getClass(), outputClass);
        }
        return outputType;
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
    public OUTPUT execute(ExecutionContext executionContext) {
        INPUT input = buildInput(executionContext);
        Set<ConstraintViolation<INPUT>> validated = ValidationUtil.validate(input);
        Assert.isTrue(CollUtil.isEmpty(validated), () -> new InputValidateException(validated));
        return execute(input, executionContext);
    }

    protected INPUT buildInput(ExecutionContext executionContext) {
        INPUT input = ReflectUtil.newInstanceIfPossible(inputClass);
        BeanUtil.fillBean(
                input,
                new ExecutionContextValueProvider(executionContext),
                DEFAULT_COPY_OPTION
        );
        return input;
    }

    public abstract OUTPUT execute(INPUT input, ExecutionContext executionContext);

    public OUTPUT execute(INPUT input) {
        return execute(input, null);
    }

}
