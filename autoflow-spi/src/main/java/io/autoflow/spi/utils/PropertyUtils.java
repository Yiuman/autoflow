package io.autoflow.spi.utils;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.OptionValueProvider;
import io.autoflow.spi.Options;
import io.autoflow.spi.Service;
import io.autoflow.spi.annotation.Cmp;
import io.autoflow.spi.annotation.Description;
import io.autoflow.spi.annotation.Select;
import io.autoflow.spi.enums.ComponentType;
import io.autoflow.spi.model.*;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2023/7/27
 */
public final class PropertyUtils {
    private static final ValidatorFactory VALIDATORFACTORY = Validation.buildDefaultValidatorFactory();

    public static final CopyOptions DEFAULT_COPY_OPTIONS = CopyOptions.create().ignoreNullValue();

    private PropertyUtils() {
    }

    public static List<Property> buildProperty(Type type) {
        return buildProperty(type, new HashMap<>());
    }

    public static String getFieldFullPath(Type type, Field field) {
        return getFieldFullPath(type, field.getName());
    }

    public static String getFieldFullPath(Type type, String fieldName) {
        return StrUtil.format("{}.{}", type.getTypeName(), fieldName);
    }

    public static <T> String getFieldFullPath(Func1<T, ?> func) {
        return getFieldFullPath(LambdaUtil.getRealClass(func), LambdaUtil.getFieldName(func));
    }

    public static List<Property> buildProperty(Type type, Map<Class<?>, List<Property>> cache) {
        Class<?> clazz = TypeUtil.getClass(type);
        if (cache.containsKey(type) || ClassUtil.isSimpleValueType(clazz)) {
            // 返回一个属性，该属性类型与当前类相同，表示递归结构
            return List.of(SimpleProperty.basicType(clazz));
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            SimpleProperty simpleProperty = new SimpleProperty();
            simpleProperty.setType(getSimpleTypeName(type));
            simpleProperty.setProperties(buildCollectionProperties(type, cache));
            return List.of(simpleProperty);
        }

        List<Property> properties = new ArrayList<>();
        Object defaultInstance = ReflectUtil.newInstanceIfPossible(clazz);
        Field[] fields = ReflectUtil.getFields(clazz, field -> !Modifier.isFinal(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers()));
        BeanDescriptor constraintsForClass = VALIDATORFACTORY.getValidator().getConstraintsForClass(clazz);
        cache.put(clazz, properties);
        for (Field field : fields) {
            Type fieldType = TypeUtil.getType(field);
            Class<?> typeClass = TypeUtil.getClass(fieldType);
            if (Objects.isNull(typeClass)) {
                continue;
            }
            SimpleProperty simpleProperty = new SimpleProperty();
            simpleProperty.setName(field.getName());
            simpleProperty.setId(getFieldFullPath(clazz, field));
            simpleProperty.setType(getSimpleTypeName(fieldType));
            simpleProperty.setComponent(buildFieldComponent(field));
            Description description = AnnotationUtil.getAnnotationValue(field, Description.class);
            if (Objects.nonNull(description)) {
                simpleProperty.setDescription(description.value());
            }

            if (!ClassUtil.isSimpleValueType(typeClass) && !Map.class.isAssignableFrom(typeClass)) {
                if (Collection.class.isAssignableFrom(typeClass)) {
                    simpleProperty.setProperties(buildCollectionProperties(fieldType, cache));
                } else {
                    simpleProperty.setProperties(buildProperty(typeClass, cache));
                }

            }

            if (Objects.nonNull(defaultInstance)) {
                simpleProperty.setDefaultValue(ReflectUtil.getFieldValue(defaultInstance, field));
            }

            simpleProperty.setValidateRules(buildValidateRules(field, constraintsForClass));
            properties.add(simpleProperty);
        }
        cache.remove(clazz);
        return properties;
    }

    private static List<Property> buildCollectionProperties(Type type, Map<Class<?>, List<Property>> cache) {
        Type[] typeArguments = TypeUtil.getTypeArguments(type);
        Class<?> childType = TypeUtil.getClass(typeArguments[0]);
        return ClassUtil.isSimpleTypeOrArray(childType)
                ? List.of(SimpleProperty.basicType(childType))
                : buildProperty(childType, cache);
    }

    @SuppressWarnings("unchecked")
    public static Component buildFieldComponent(Field field) {
        Class<?> typeClass = field.getType();
        if (typeClass.isEnum()) {
            List<String> names = EnumUtil.getNames((Class<? extends Enum<?>>) typeClass);
            List<Option> options = names.stream().map(enumName -> {
                Option option = new Option();
                option.setName(enumName);
                option.setValue(enumName);
                return option;
            }).toList();


            Component component = new Component();
            component.setType(ComponentType.Select);
            component.setProps(Map.of("options", options));
            return component;
        }

        Annotation[] annotations = AnnotationUtil.getAnnotations(field, true);

        return Arrays.stream(annotations).map(annotation -> {
                    if (annotation instanceof Select selectAnn) {

                        Map<String, Object> props = new HashMap<>();

                        // 1. 先尝试 provider
                        Class<? extends OptionValueProvider> providerClass = selectAnn.provider();
                        List<Option> options;

                        if (providerClass != OptionValueProvider.class) {
                            try {
                                options = Options.getOptions(providerClass);
                            } catch (Exception e) {
                                throw new RuntimeException("无法实例化 SelectOptionsProvider: " + providerClass, e);
                            }
                        } else {
                            options = Arrays.stream(selectAnn.options()).map(option -> new Option(option, option))
                                    .collect(Collectors.toList());
                        }

                        props.put("options", options);
                        // 3. defaultValue
                        if (!selectAnn.defaultValue().isEmpty()) {
                            props.put("defaultValue", selectAnn.defaultValue());
                        }

                        Component component = new Component();
                        component.setType(ComponentType.Select);
                        component.setProps(props);
                        return component;
                    }

                    Cmp cmp;
                    if (Cmp.class.equals(annotation.annotationType())) {
                        cmp = (Cmp) annotation;
                    } else {
                        cmp = AnnotationUtil.getAnnotation(annotation.annotationType(), Cmp.class);
                    }

                    if (Objects.isNull(cmp)) {
                        return null;
                    }

                    return Component.of(cmp.value(), AnnotationUtil.getAnnotationValueMap(field, annotation.annotationType()));
                }).filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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

    public static <SERVICE extends Service<?>> List<Property> buildProperties(Class<SERVICE> serviceClass, Type type) {
        try {
            String propertiesJsonFile = StrUtil.format("{}_properties.json", serviceClass.getName());
            String propertiesJsonStr = ResourceUtil.readUtf8Str(propertiesJsonFile);
            if (StrUtil.isNotBlank(propertiesJsonStr)) {
                return JSONUtil.toList(propertiesJsonStr, Property.class);
            }
        } catch (cn.hutool.core.io.resource.NoResourceException ignore) {
        }

        return PropertyUtils.buildProperty(type);
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

    public static void modifyProperties(List<? extends Property> properties, Consumer<Property> consumer) {
        if (CollUtil.isEmpty(properties)) {
            return;
        }

        for (Property property : properties) {
            consumer.accept(property);
            if (CollUtil.isNotEmpty(property.getProperties())) {
                modifyProperties(property.getProperties(), consumer);
            }
        }
    }

    public static <T> Map<String, T> nameValuesToMap(List<NamedValue<T>> namedValues) {
        if (CollUtil.isEmpty(namedValues)) {
            return MapUtil.empty();
        }

        return namedValues.stream()
                .collect(Collectors.toMap(NamedValue::getName, NamedValue::getValue));
    }
}


