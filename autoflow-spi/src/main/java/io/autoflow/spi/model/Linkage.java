package io.autoflow.spi.model;

import cn.hutool.core.map.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 联动
 * 基于value的值动态处理参数
 *
 * @param <DATA> 联动
 * @author yiuman
 * @date 2024/9/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Linkage<DATA> {
    private DATA value;
    private Map<String, Object> parameter;

    public Linkage(DATA value) {
        this.value = value;
    }

    public static <DATA> Linkage<DATA> from(DATA value) {
        return new Linkage<>(value, MapUtil.empty());
    }
}
