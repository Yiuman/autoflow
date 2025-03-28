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
    private String requestId;
    private String name;
    private List<Node> nodes;
    private List<Connection> connections;
    private String description;
    private String parentId;
    private Map<String, Object> data;

    /**
     * 下面这两个属性只读
     */
    private Set<String> connectionSources;
    private Set<String> connectionTargets;

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
        Set<String> connectionTargetList = getConnectionTargets();
        List<Node> startNodes = getNodes().stream()
                .filter(node -> !connectionTargetList.contains(node.getId()))
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
        return getOutgoers(node, true);
    }

    public List<Node> getOutgoers(Node node, boolean deep) {
        return getOutgoers(node.getId(), deep);
    }


    public List<Node> getOutgoers(String nodeId) {
        return getOutgoers(nodeId, true);
    }

    public List<Node> getOutgoers(String nodeId, boolean deep) {
        List<String> outgoerIds = getOutgoerIds(nodeId, deep);
        return nodes.stream()
                .filter(nodeItem -> outgoerIds.contains(nodeItem.getId()))
                .sorted(Comparator.comparingInt(o -> outgoerIds.indexOf(o.getId())))
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
        return getOutgoerIds(nodeId, matchConnect, true);
    }

    public List<String> getOutgoerIds(String nodeId, boolean deep) {
        return getOutgoerIds(nodeId, c -> true, deep);
    }

    public List<String> getOutgoerIds(String nodeId, Predicate<Connection> matchConnect, boolean deep) {
        if (CollUtil.isEmpty(connections)) {
            return CollUtil.newArrayList();
        }
        List<String> list = connections
                .stream()
                .filter(connection -> Objects.equals(connection.getSource(), nodeId) && matchConnect.test(connection))
                .map(Connection::getTarget)
                .collect(Collectors.toList());
        if (!deep) {
            return list;
        }
        List<String> outgoerIds = new ArrayList<>(list);
        if (CollUtil.isNotEmpty(list)) {
            for (String outgoerId : list) {
                outgoerIds.addAll(getOutgoerIds(outgoerId));
            }

        }

        return outgoerIds;
    }

    private void findAllOutgoers(List<String> allOutgoers, List<String> currentOutgoers, Set<String> visited, Predicate<Connection> matchConnect) {
        for (String outgoerId : currentOutgoers) {
            if (!visited.contains(outgoerId)) {
                visited.add(outgoerId);
                allOutgoers.add(outgoerId);

                List<String> nextOutgoers = connections.stream()
                        .filter(connection -> Objects.equals(connection.getSource(), outgoerId) && matchConnect.test(connection))
                        .map(Connection::getTarget)
                        .collect(Collectors.toList());

                findAllOutgoers(allOutgoers, nextOutgoers, visited, matchConnect);
            }
        }
    }

    public List<Connection> getNodeConnections(Node node) {
        return connections.stream().filter(connection ->
                        Objects.equals(connection.getSource(), node.getId())
                                || Objects.equals(connection.getTarget(), node.getId()))
                .collect(Collectors.toList());
    }
}
