package io.autoflow.liteflow.cmp;

import lombok.Data;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Data
public class ServiceData {
    private String serviceId;
    private Map<String, Object> parameters;
}
