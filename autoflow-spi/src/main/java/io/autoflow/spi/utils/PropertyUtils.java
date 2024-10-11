package io.autoflow.spi.utils;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.*;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.Service;
import io.autoflow.spi.model.*;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
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

    public static <T> List<Property> buildProperty(Class<T> clazz) {
        return buildProperty(clazz, new HashMap<>());
    }

    public static String getFieldFullPath(Class<?> clazz, Field field) {
        return getFieldFullPath(clazz, field.getName());
    }

    public static String getFieldFullPath(Class<?> clazz, String fieldName) {
        return StrUtil.format("{}.{}", clazz.getName(), fieldName);
    }

    public static <T> String getFieldFullPath(Func1<T, ?> func) {
        return getFieldFullPath(LambdaUtil.getRealClass(func), LambdaUtil.getFieldName(func));
    }

    public static <T> List<Property> buildProperty(Class<T> clazz, Map<Class<?>, List<Property>> cache) {
        if (cache.containsKey(clazz) || ClassUtil.isSimpleValueType(clazz)) {
            // 返回一个属性，该属性类型与当前类相同，表示递归结构
            return List.of(SimpleProperty.basicType(clazz));
        }

        List<Property> properties = new ArrayList<>();
        T defaultInstance = ReflectUtil.newInstanceIfPossible(clazz);
        Field[] fields = ReflectUtil.getFields(clazz, field -> !Modifier.isFinal(field.getModifiers()));
        BeanDescriptor constraintsForClass = VALIDATORFACTORY.getValidator().getConstraintsForClass(clazz);
        cache.put(clazz, properties);
        for (Field field : fields) {
            Type type = TypeUtil.getType(field);
            Class<?> typeClass = TypeUtil.getClass(type);
            if (Objects.isNull(typeClass)) {
                continue;
            }
            SimpleProperty simpleProperty = new SimpleProperty();
            simpleProperty.setName(field.getName());
            simpleProperty.setId(getFieldFullPath(clazz, field));
            simpleProperty.setType(getSimpleTypeName(type));
            simpleProperty.setOptions(buildFieldOptions(field));
            Description description = AnnotationUtil.getAnnotationValue(field, Description.class);
            if (Objects.nonNull(description)) {
                simpleProperty.setDescription(description.value());
            }

            if (!ClassUtil.isSimpleValueType(typeClass) && !Map.class.isAssignableFrom(typeClass)) {
                if (Collection.class.isAssignableFrom(typeClass)) {
                    Type[] typeArguments = TypeUtil.getTypeArguments(type);
                    Class<?> childType = TypeUtil.getClass(typeArguments[0]);
                    List<Property> propertyList = ClassUtil.isSimpleTypeOrArray(childType)
                            ? List.of(SimpleProperty.basicType(childType))
                            : buildProperty(childType, cache);
                    simpleProperty.setProperties(propertyList);

                } else {
                    simpleProperty.setProperties(buildProperty(typeClass, cache));
                }

            }

            simpleProperty.setDefaultValue(ReflectUtil.getFieldValue(defaultInstance, field));
            simpleProperty.setValidateRules(buildValidateRules(field, constraintsForClass));
            properties.add(simpleProperty);
        }
        cache.remove(clazz);
        return properties;
    }

    @SuppressWarnings("unchecked")
    public static List<Option> buildFieldOptions(Field field) {
        Class<?> typeClass = field.getType();
        if (typeClass.isEnum()) {
            List<String> names = EnumUtil.getNames((Class<? extends Enum<?>>) typeClass);
            return names.stream().map(enumName -> {
                Option option = new Option();
                option.setName(enumName);
                option.setValue(enumName);
                return option;
            }).collect(Collectors.toList());
        }

        OptionValues annotation = AnnotationUtil.getAnnotation(field, OptionValues.class);
        if (Objects.nonNull(annotation)) {
            String[] value = annotation.value();
            return Arrays.stream(value).map(valueStr -> {
                Option option;
                try {
                    option = JSONUtil.toBean(valueStr, Option.class);
                } catch (Throwable throwable) {
                    option = new Option();
                    option.setName(valueStr);
                    option.setValue(valueStr);
                }

                return option;
            }).collect(Collectors.toList());
        }
        return null;
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

    public static <SERVICE extends Service<?>> List<Property> buildProperies(Class<SERVICE> serviceClass, Class<?> inputClass) {
        try {
            String propertiesJsonFile = StrUtil.format("{}_properties.json", serviceClass.getName());
            String propertiesJsonStr = ResourceUtil.readUtf8Str(propertiesJsonFile);
            if (StrUtil.isNotBlank(propertiesJsonStr)) {
                return JSONUtil.toList(propertiesJsonStr, Property.class);
            }
        } catch (cn.hutool.core.io.resource.NoResourceException ignore) {
        }

        return PropertyUtils.buildProperty(inputClass);
    }

    public static String getSimpleTypeName(Type type) {
        if (type instanceof Class) {
            // 普通类型直接获取 simpleName
            return ((Class<?>) type).getSimpleName();
        } else if (type instanceof ParameterizedType paramType) {
            // 获取主类型
            Type rawType = paramType.getRawType();
            // 获取泛型参数
            Type[] typeArgs = paramType.getActualTypeArguments();
            StringBuilder typeName = new StringBuilder();
            if (rawType instanceof Class) {
                typeName.append(((Class<?>) rawType).getSimpleName());
            }

            // 处理泛型参数
            if (typeArgs.length > 0) {
                typeName.append("<");
                for (int i = 0; i < typeArgs.length; i++) {
                    if (i > 0) {
                        typeName.append(", ");
                    }
                    typeName.append(getSimpleTypeName(typeArgs[i]));
                }
                typeName.append(">");
            }

            return typeName.toString();
        }

        // 如果不是 Class 或 ParameterizedType，返回全路径类型名
        return type.getTypeName();
    }
}


