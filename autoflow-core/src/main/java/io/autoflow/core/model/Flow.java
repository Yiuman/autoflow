package io.autoflow.core.model;

import lombok.Data;

import java.util.List;

/**
 * 流程
 *
 * @author yiuman
 * @date 2023/7/13
 */
@Data
public class Flow {
    private String id;
    private String name;
    private List<Node> nodes;
    private List<Connection> connections;
}
