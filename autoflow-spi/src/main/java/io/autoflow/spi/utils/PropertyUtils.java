package io.autoflow.spi.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ReflectUtil;
import io.autoflow.spi.model.Option;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.model.SimpleProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2023/7/27
 */
public final class PropertyUtils {
    private PropertyUtils() {
    }

    @SuppressWarnings("unchecked")
    public static List<Property> buildProperty(Class<?> clazz) {
        List<Property> properties = new ArrayList<>();
        Field[] fields = ReflectUtil.getFields(clazz, field -> !Modifier.isFinal(field.getModifiers()));
        for (Field field : fields) {
            Class<?> type = field.getType();
            SimpleProperty simpleProperty = new SimpleProperty();
            simpleProperty.setName(field.getName());
            simpleProperty.setType(type.getSimpleName());
            if (type.isEnum()) {
                List<String> names = EnumUtil.getNames((Class<? extends Enum<?>>) type);
                List<Option> options = names.stream().map(enumName -> {
                    Option option = new Option();
                    option.setName(enumName);
                    option.setValue(enumName);
                    return option;
                }).collect(Collectors.toList());
                simpleProperty.setOptions(options);
            }
            if (!ClassUtil.isSimpleValueType(type) && !Map.class.isAssignableFrom(type)) {
                simpleProperty.setType(type.getSimpleName());
                simpleProperty.setProperties(buildProperty(type));
            }

            properties.add(simpleProperty);
        }
        return properties;
    }
}
