package io.autoflow.spi.model;

import cn.hutool.core.map.MapUtil;
import io.autoflow.spi.enums.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/11/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Component {
    private ComponentType type;
    private Map<String, Object> props;

    public static Component of(ComponentType type) {
        return new Component(type, MapUtil.empty());
    }

    public static Component of(ComponentType type, Map<String, Object> props) {
        return new Component(type, props);
    }
}
