package io.autoflow.plugin.llm;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.model.SimpleProperty;
import io.autoflow.spi.utils.PropertyUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yiuman
 * @date 2024/9/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ModelConfig extends Model {
    private String propertyClass;
    private List<Property> properties;
    private Map<String, SimpleProperty> overwriteProperty;

    public List<Property> getProperties() {
        if (Objects.isNull(properties) && StrUtil.isNotBlank(propertyClass)) {
            try {
                properties = PropertyUtils.buildProperty(Class.forName(propertyClass));
            } catch (ClassNotFoundException ignore) {
            }
        }

        if (Objects.nonNull(overwriteProperty)) {
            PropertyUtils.modifyProperties(properties, property -> {
                Property matchOverwrite = overwriteProperty.get(property.getId());
                if (Objects.nonNull(matchOverwrite)) {
                    BeanUtil.copyProperties(matchOverwrite, property, PropertyUtils.DEFAULT_COPY_OPTIONS);
                }
            });
        }


        return properties;
    }
}
