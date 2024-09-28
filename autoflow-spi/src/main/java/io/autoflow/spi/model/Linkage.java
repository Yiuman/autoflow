package io.autoflow.spi.model;

import lombok.Data;

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
public class Linkage<DATA> {
    private DATA value;
    private Map<String, Object> parameter;
}
