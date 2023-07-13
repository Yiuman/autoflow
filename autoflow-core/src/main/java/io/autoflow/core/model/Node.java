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
    private Map<String, Object> parameters;
    private Float[] position;
    private NodeType type;
}
