package io.autoflow.core.model;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private String description;

    private Set<String> connectionSources;
    private Set<String> connectionTargets;

    public Set<String> getConnectionSources() {
        if (CollUtil.isEmpty(connections)) {
            return Collections.emptySet();
        }
        return connections.stream().map(Connection::getSource).collect(Collectors.toSet());
    }

    public Set<String> getConnectionTargets() {
        if (CollUtil.isEmpty(connections)) {
            return Collections.emptySet();
        }
        return connections.stream().map(Connection::getTarget).collect(Collectors.toSet());
    }

    public List<Node> getStartNodes() {
        return getNodes().stream()
                .filter(node -> !getConnectionTargets().contains(node.getId())
                        && getConnectionSources().contains(node.getId())).collect(Collectors.toList());
    }

    public List<Node> getEndNodes() {
        return getNodes().stream()
                .filter(node -> !getConnectionSources().contains(node.getId())
                        && getConnectionTargets().contains(node.getId())).collect(Collectors.toList());
    }
}
