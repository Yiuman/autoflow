package io.autoflow.spi.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import io.autoflow.spi.model.Option;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.model.SimpleProperty;
import io.autoflow.spi.model.ValidateRule;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2023/7/27
 */
public final class PropertyUtils {
    private static final ValidatorFactory VALIDATORFACTORY = Validation.buildDefaultValidatorFactory();

    private PropertyUtils() {
    }

    @SuppressWarnings("unchecked")
    public static List<Property> buildProperty(Class<?> clazz) {
        List<Property> properties = new ArrayList<>();
        Field[] fields = ReflectUtil.getFields(clazz, field -> !Modifier.isFinal(field.getModifiers()));
        BeanDescriptor constraintsForClass = VALIDATORFACTORY.getValidator().getConstraintsForClass(clazz);
        for (Field field : fields) {
            Type type = TypeUtil.getType(field);
            Class<?> typeClass = TypeUtil.getClass(type);
            SimpleProperty simpleProperty = new SimpleProperty();
            simpleProperty.setName(field.getName());
            simpleProperty.setType(typeClass.getSimpleName());
            if (typeClass.isEnum()) {
                List<String> names = EnumUtil.getNames((Class<? extends Enum<?>>) type);
                List<Option> options = names.stream().map(enumName -> {
                    Option option = new Option();
                    option.setName(enumName);
                    option.setValue(enumName);
                    return option;
                }).collect(Collectors.toList());
                simpleProperty.setOptions(options);
            }
            if (!ClassUtil.isSimpleValueType(typeClass) && !Map.class.isAssignableFrom(typeClass)) {
                if (Collection.class.isAssignableFrom(typeClass)) {
                    Type[] typeArguments = TypeUtil.getTypeArguments(type);
                    simpleProperty.setProperties(buildProperty(TypeUtil.getClass(typeArguments[0])));
                } else {
                    simpleProperty.setProperties(buildProperty(typeClass));
                }

            }

            simpleProperty.setValidateRules(buildValidateRules(field, constraintsForClass));
            properties.add(simpleProperty);
        }
        return properties;
    }

    public static List<ValidateRule> buildValidateRules(Field field, BeanDescriptor constraintsForClass) {
        MessageInterpolator messageInterpolator = VALIDATORFACTORY.getMessageInterpolator();
        PropertyDescriptor constraintsForProperty = constraintsForClass.getConstraintsForProperty(field.getName());
        if (Objects.nonNull(constraintsForProperty)) {
            Set<ConstraintDescriptor<?>> constraintDescriptors = constraintsForProperty.getConstraintDescriptors();
            return constraintDescriptors.stream().map(constraintDescriptor -> {
                ValidateRule validateRule = new ValidateRule();
                validateRule.setField(field.getName());
                validateRule.setRequired(true);
                String message = messageInterpolator.interpolate(constraintDescriptor.getMessageTemplate(), new MessageInterpolator.Context() {
                    @Override
                    public ConstraintDescriptor<?> getConstraintDescriptor() {
                        return constraintDescriptor;
                    }

                    @Override
                    public Object getValidatedValue() {
                        return null;
                    }

                    @Override
                    public <T> T unwrap(Class<T> type) {
                        return type.cast(this);
                    }
                });
                validateRule.setMessage(constraintsForProperty.getPropertyName() + message);
                validateRule.setFieldType(field.getType().getSimpleName());
                validateRule.setValidateType(constraintDescriptor.getAnnotation().annotationType().getSimpleName());
                validateRule.setAttributes(constraintDescriptor.getAttributes());
                return validateRule;
            }).toList();
        }
        return null;
    }
}
