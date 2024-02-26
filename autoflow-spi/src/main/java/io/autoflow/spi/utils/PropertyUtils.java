package io.autoflow.spi.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.model.SimpleProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/27
 */
public final class PropertyUtils {
    private PropertyUtils() {
    }

    public static List<Property> buildProperty(Class<?> clazz) {
        List<Property> properties = new ArrayList<>();
        Field[] fields = ReflectUtil.getFields(clazz);
        for (Field field : fields) {
            Class<?> type = field.getType();
            SimpleProperty simpleProperty = new SimpleProperty();
            simpleProperty.setName(field.getName());
            simpleProperty.setType(type.getSimpleName());
            if (!ClassUtil.isBasicType(type)) {
                simpleProperty.setType(Object.class.getSimpleName());
                simpleProperty.setProperties(buildProperty(type));
            }

            properties.add(simpleProperty);
        }
        return properties;
    }
}
