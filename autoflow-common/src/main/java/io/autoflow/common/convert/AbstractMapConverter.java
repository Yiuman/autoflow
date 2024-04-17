package io.autoflow.common.convert;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.AbstractConverter;
import cn.hutool.core.convert.ConverterRegistry;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;

import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @param <KEY>   键类型
 * @param <VALUE> 值类型
 * @author yiuman
 * @date 2024/4/17
 */
public abstract class AbstractMapConverter<KEY, VALUE> extends AbstractConverter<Map<KEY, VALUE>> {

    /**
     * Map类型
     */
    private final Type mapType;
    /**
     * 键类型
     */
    private final Type keyType;
    /**
     * 值类型
     */
    private final Type valueType;

    public AbstractMapConverter() {
        this.mapType = LinkedHashMap.class;
        this.keyType = TypeUtil.getTypeArgument(getClass(), 0);
        this.valueType = TypeUtil.getTypeArgument(getClass(), 1);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Map<KEY, VALUE> convertInternal(Object value) {
        Map map;
        if (value instanceof Map) {
            final Class<?> valueClass = value.getClass();
            if (valueClass.equals(this.mapType)) {
                final Type[] typeArguments = TypeUtil.getTypeArguments(valueClass);
                if (null != typeArguments //
                        && 2 == typeArguments.length//
                        && Objects.equals(this.keyType, typeArguments[0]) //
                        && Objects.equals(this.valueType, typeArguments[1])) {
                    //对于键值对类型一致的Map对象，不再做转换，直接返回原对象
                    return (Map) value;
                }
            }

            final Class<?> mapClass = TypeUtil.getClass(this.mapType);
            if (null == mapClass || mapClass.isAssignableFrom(AbstractMap.class)) {
                // issue#I6YN2A，默认有序
                map = new LinkedHashMap<>();
            } else {
                map = MapUtil.createMap(mapClass);
            }
            convertMapToMap((Map) value, map);
        } else if (BeanUtil.isBean(value.getClass())) {
            map = BeanUtil.beanToMap(value);
            // 二次转换，转换键值类型
            map = convertInternal(map);
        } else {
            throw new UnsupportedOperationException(StrUtil.format("Unsupported toMap value type: {}", value.getClass().getName()));
        }
        return map;
    }

    /**
     * Map转Map
     *
     * @param srcMap    源Map
     * @param targetMap 目标Map
     */
    private void convertMapToMap(Map<?, ?> srcMap, Map<Object, Object> targetMap) {
        final ConverterRegistry convert = ConverterRegistry.getInstance();
        srcMap.forEach((key, value) -> {
            key = TypeUtil.isUnknown(this.keyType) ? key : convert.convert(this.keyType, key);
            value = TypeUtil.isUnknown(this.valueType) ? value : convert.convert(this.valueType, value);
            targetMap.put(key, value);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<KEY, VALUE>> getTargetType() {
        return (Class<Map<KEY, VALUE>>) TypeUtil.getClass(this.mapType);
    }
}
