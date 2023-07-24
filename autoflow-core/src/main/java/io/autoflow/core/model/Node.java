package io.autoflow.core.model;

import lombok.Data;

import java.util.Map;

/**
 * 节点
 *
 * @author yiuman
 * @date 2023/7/13
 */
@Data
public class Node {
    private String id;
    private String name;
    /**
     * 节点的静态配置参数
     */
    private Map<String, Object> parameters;
    /**
     * 节点在前端显示的位置定位
     */
    private Float[] position;
    /**
     * 节点类型
     */
    private NodeType type;
    /**
     * 实现类（即插件）
     */
    private String serviceName;
}
