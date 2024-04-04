package io.autoflow.core.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    /**
     * 下面这两个属性只读
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<String> connectionSources;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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
        List<Node> startNodes = getNodes().stream()
                .filter(node -> !getConnectionTargets().contains(node.getId())
                        && getConnectionSources().contains(node.getId()))
                .collect(Collectors.toList());
        return CollUtil.isNotEmpty(startNodes) ? startNodes : getNodes();
    }

    public List<Node> getEndNodes() {
        List<Node> endNodes = getNodes().stream()
                .filter(node -> !getConnectionSources().contains(node.getId())
                        && getConnectionTargets().contains(node.getId()))
                .collect(Collectors.toList());
        return CollUtil.isNotEmpty(endNodes) ? endNodes : getNodes();
    }

    public static Flow singleNodeFlow(Node node){
        Flow flow = new Flow();
        String flowId = StrUtil.format("single_node_{}", node.getId());
        flow.setId(flowId);
        flow.setName(flowId);
        flow.setNodes(List.of(node));
        return flow;
    }
}
