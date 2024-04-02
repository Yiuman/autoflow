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
    private String label;
    /**
     * 节点的静态配置参数
     */
    private Map<String, Object> data;
    /**
     * 节点在前端显示的位置定位
     */
    private Position position;
    /**
     * 节点类型
     */
    private NodeType type;
    /**
     * 实现类（即插件）
     */
    private String serviceId;
    /**
     * 循环参数
     */
    private Loop loop;
}
