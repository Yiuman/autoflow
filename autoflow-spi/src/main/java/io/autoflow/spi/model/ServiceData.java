package io.autoflow.spi.model;

import lombok.Data;

import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Data
public class ServiceData {
    /**
     * 流程ID
     */
    private String flowId;
    /**
     * 节点ID
     */
    private String nodeId;
    /**
     * 服务插件ID
     */
    private String serviceId;
    /**
     * 流程实例ID
     */
    private String flowInstId;
    /**
     * 参数
     */
    private Map<String, Object> parameters;
}
