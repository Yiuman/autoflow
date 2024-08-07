package io.autoflow.spi.model;

import lombok.Data;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Data
public class ServiceData {
    private String flowId;
    private String nodeId;
    private String serviceId;
    private Map<String, Object> parameters;
}
