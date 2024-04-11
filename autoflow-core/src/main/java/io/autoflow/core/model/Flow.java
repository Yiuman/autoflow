package io.autoflow.core.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.*;
import java.util.function.Predicate;
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
    private Set<String> connectionSources;
    private Set<String> connectionTargets;

    /**
     * key为nodeId,value为从属的流程ID
     * 因为node可能是子流程
     */
    private Map<String, String> nodeDependentFlowCache = new HashMap<>();

    public static Flow singleNodeFlow(Node node) {
        Flow flow = new Flow();
        String flowId = StrUtil.format("single_node_{}", node.getId());
        flow.setId(flowId);
        flow.setName(flowId);
        flow.setNodes(List.of(node));
        return flow;
    }

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

    public List<Node> getStartNodes(String flowId) {
        if (Objects.equals(id, flowId)) {
            return getStartNodes();
        }
        List<Node> outgoers = getOutgoers(flowId);
        return outgoers.stream()
                .filter(node -> getConnectionTargets().contains(flowId))
                .collect(Collectors.toList());
    }

    public List<Node> getEndNodes() {
        List<Node> endNodes = getNodes().stream()
                .filter(node ->
                        !getConnectionSources().contains(node.getId())
                                && getConnectionTargets().contains(node.getId()))
                .collect(Collectors.toList());
        return CollUtil.isNotEmpty(endNodes) ? endNodes : getNodes();
    }

    public List<Node> getEndNodes(String flowId) {
        if (Objects.equals(id, flowId)) {
            return getEndNodes();
        }
        List<Node> outgoers = getOutgoers(flowId);
        return outgoers.stream()
                .filter(node -> !getConnectionSources().contains(node.getId())
                        && getConnectionTargets().contains(node.getId()))
                .collect(Collectors.toList());
    }

    public String getDependentFlowId(String nodeId) {
        String dependentFlowId = nodeDependentFlowCache.get(nodeId);
        if (StrUtil.isNotBlank(dependentFlowId)) {
            return dependentFlowId;
        }

        List<Node> collect = getIncomers(nodeId).stream()
                .filter(nodeItem -> NodeType.SUBFLOW == nodeItem.getType())
                .collect(Collectors.toList());
        Node last = CollUtil.getLast(collect);
        dependentFlowId = Objects.nonNull(last) ? last.getId() : id;
        nodeDependentFlowCache.put(nodeId, dependentFlowId);
        return nodeDependentFlowCache.get(nodeId);
    }

    public String getDependentFlowId(Node node) {
        return getDependentFlowId(node.getId());
    }

    public List<Node> getIncomers(Node node) {
        return getIncomers(node.getId());
    }

    public List<Node> getIncomers(String nodeId) {
        List<String> incomerIds = getIncomerIds(nodeId);
        return nodes.stream()
                .filter(nodeItem -> incomerIds.contains(nodeItem.getId()))
                .collect(Collectors.toList());
    }

    public List<String> getIncomerIds(String nodeId) {
        if (CollUtil.isEmpty(connections)) {
            return CollUtil.newArrayList();
        }
        List<String> list = connections.stream()
                .filter(connection -> Objects.equals(connection.getTarget(), nodeId))
                .map(Connection::getSource)
                .toList();
        List<String> incomerIds = new ArrayList<>(list);
        if (CollUtil.isNotEmpty(list)) {
            for (String incomerId : list) {
                incomerIds.addAll(getIncomerIds(incomerId));
            }

        }
        return CollUtil.reverse(incomerIds);
    }

    public List<Node> getOutgoers(Node node) {
        return getOutgoers(node.getId());
    }


    public List<Node> getOutgoers(String nodeId) {
        List<String> outgoerIds = getOutgoerIds(nodeId);
        return nodes.stream()
                .filter(nodeItem -> outgoerIds.contains(nodeItem.getId()))
                .collect(Collectors.toList());
    }

    public List<Node> getOutgoers(String nodeId, Predicate<Connection> matchConnect) {
        List<String> outgoerIds = getOutgoerIds(nodeId, matchConnect);
        return nodes.stream()
                .filter(nodeItem -> outgoerIds.contains(nodeItem.getId()))
                .collect(Collectors.toList());
    }

    public List<String> getOutgoerIds(String nodeId) {
        return getOutgoerIds(nodeId, c -> true);
    }

    public List<String> getOutgoerIds(String nodeId, Predicate<Connection> matchConnect) {
        if (CollUtil.isEmpty(connections)) {
            return CollUtil.newArrayList();
        }
        List<String> list = connections
                .stream()
                .filter(connection -> Objects.equals(connection.getSource(), nodeId) && matchConnect.test(connection))
                .map(Connection::getTarget)
                .collect(Collectors.toList());
        List<String> outgoerIds = new ArrayList<>(list);
        if (CollUtil.isNotEmpty(list)) {
            for (String outgoerId : list) {
                outgoerIds.addAll(getOutgoerIds(outgoerId));
            }

        }
        return CollUtil.reverse(outgoerIds);

    }

    public List<Connection> getNodeConnections(Node node) {
        return connections.stream().filter(connection ->
                        Objects.equals(connection.getSource(), node.getId())
                                || Objects.equals(connection.getTarget(), node.getId()))
                .collect(Collectors.toList());
    }
}
