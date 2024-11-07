package io.autoflow.liteflow.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yomahub.liteflow.builder.LiteFlowNodeBuilder;
import com.yomahub.liteflow.builder.el.*;
import com.yomahub.liteflow.core.NodeComponent;
import io.autoflow.core.enums.PointType;
import io.autoflow.core.model.Connection;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.model.NodeType;
import io.autoflow.liteflow.cmp.IFNodeComponent;
import io.autoflow.liteflow.cmp.LoopNodeComponent;
import io.autoflow.liteflow.cmp.ServiceNodeComponent;
import io.autoflow.plugin.loopeachitem.LoopItem;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import io.autoflow.spi.model.ServiceData;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Slf4j
public final class LiteFlows {
    private LiteFlows() {
    }

    public static String createChain(Flow flow) {
        LiteFlowChainELBuilder.createChain()
                .setChainId(flow.getId())
                .setEL(convertElStr(flow))
                .build();
        return flow.getId();
    }

    public static String convertElStr(Flow flow) {
        ELWrapper elWrapper = convertEl(flow);
        if (elWrapper instanceof NodeELWrapper) {
            return ELBus.then(elWrapper).toEL(true);
        }
        String el = elWrapper.toEL(true);
        log.debug("FLOW【{} - {}】- EL  {} ", flow.getName(), flow.getId(), el);
        return el;
    }


    // 处理每个节点
    private static ELWrapper processNode(Flow flow, Node node) {
        buildNode(node);
        if (NodeType.LOOP_EACH_ITEM == node.getType()) {
            return createIteratorEL(node, flow); // 处理循环节点
        } else if (NodeType.IF == node.getType()) {
            return processIfNode(flow, node); // 处理条件节点
        } else {
            return createServiceNodeEL(flow, node); // 处理默认节点
        }
    }

    private static ELWrapper processIfNode(Flow flow, Node node) {
        if (CollUtil.isEmpty(flow.getConnections())) {
            return createServiceNodeEL(flow, node); // 无连接时仅添加服务节点
        } else {
            return ELBus.then(createServiceNodeEL(flow, node), createIfNodeEl(node, flow));
        }
    }

    // 创建下一个Flow
    private static Flow createNextFlow(String nextFlowId, List<Node> outgoers, Flow flow, Node node) {
        Flow nextFlow = new Flow();
        nextFlow.setId(nextFlowId);
        nextFlow.setNodes(outgoers);
        nextFlow.setConnections(getOutgoerConnections(flow, node, outgoers)); // 获取连接
        return nextFlow; // 返回下一个Flow
    }


    private static Map<String, List<Node>> getStartNodeOutgoers(Flow flow, List<Node> startNodes) {
        return startNodes.stream()
                .collect(Collectors.toMap(Node::getId, flow::getOutgoers));
    }

    private static Node findCommonNode(Map<String, List<Node>> startNodeIdOutgoerMap) {
        List<Node> commonNodes = new ArrayList<>(startNodeIdOutgoerMap.values().iterator().next());
        for (List<Node> outgoers : startNodeIdOutgoerMap.values()) {
            commonNodes.retainAll(outgoers);
        }
        return CollUtil.getFirst(commonNodes);
    }

    private static Map<Node, Set<String>> findJointNodes(Map<String, List<Node>> startNodeIdOutgoerMap) {
        Map<Node, Set<String>> jointNodes = new HashMap<>();
        List<String> keys = new ArrayList<>(startNodeIdOutgoerMap.keySet());

        for (int i = 0; i < keys.size(); i++) {
            for (int j = i + 1; j < keys.size(); j++) {
                String key1 = keys.get(i);
                String key2 = keys.get(j);
                List<Node> outgoers1 = startNodeIdOutgoerMap.get(key1);
                List<Node> outgoers2 = startNodeIdOutgoerMap.get(key2);

                for (Node node : outgoers2) {
                    if (outgoers1.contains(node)) {
                        Set<String> startKeys = jointNodes.get(node);
                        if (Objects.isNull(startKeys)) {
                            startKeys = new HashSet<>();
                        }
                        startKeys.add(key1);
                        startKeys.add(key2);
                        jointNodes.put(node, startKeys);
                        break;
                    }
                }
            }
        }

        return jointNodes;
    }

    private static Flow createSubFlow(Flow flow, Node head, List<Node> outgoers) {
        Flow flowItem = new Flow();
        flowItem.setId("sub_flow_" + head.getId());
        List<Node> nodes = new ArrayList<>();
        nodes.add(head);
        nodes.addAll(outgoers);
        flowItem.setNodes(nodes);
        List<Connection> allConnections = flow.getConnections();
        List<Connection> connections = nodes.stream()
                .flatMap(node -> allConnections.stream()
                        .filter(connection -> Objects.equals(connection.getSource(), node.getId())
                                || Objects.equals(connection.getTarget(), node.getId())))
                .filter(connection -> !Objects.equals(connection.getTarget(), head.getId()))
                .toList();
        flowItem.setConnections(connections);
        return flowItem;
    }

    private static Flow createJointFlow(Flow flow, List<Node> jointStartNodes,
                                        Map<String, List<Node>> outgoerMap,
                                        Node jointNode
    ) {
        Set<Node> nodes = new HashSet<>();
        for (Node jointStartNode : jointStartNodes) {
            nodes.add(jointStartNode);
            List<Node> currentNodeOutgoers = outgoerMap.get(jointStartNode.getId());
            if (Objects.nonNull(jointNode)) {
                currentNodeOutgoers = currentNodeOutgoers.subList(0, currentNodeOutgoers.indexOf(jointNode));
            }
            nodes.addAll(currentNodeOutgoers);
        }
        List<String> startNodeIds = jointStartNodes.stream()
                .map(Node::getId).toList();
        String flowId = String.join("$", startNodeIds);
        Flow subflow = new Flow();
        subflow.setId("sub_flow_" + flowId);
        subflow.setNodes(new ArrayList<>(nodes));
        List<Connection> connectionList = nodes.stream()
                .flatMap(node -> flow.getNodeConnections(node).stream())
                .filter(connection -> !startNodeIds.contains(connection.getTarget()))
                .toList();
        subflow.setConnections(connectionList);
        return subflow;
    }

    private static ELWrapper handleNoCommonNode(Flow flow,
                                                List<Node> startNodes,
                                                Map<String, List<Node>> startNodeOutgoers) {
        List<ELWrapper> whenElList = new ArrayList<>();
        Map<Node, Set<String>> jointNodes = findJointNodes(startNodeOutgoers);
        //有交集的节点ID集合
        List<String> startNodeKeys = jointNodes.values()
                .stream().flatMap(Collection::stream)
                .distinct()
                .toList();

        //无交集节点
        List<Node> unJoinStartNodes = startNodes.stream()
                .filter(startNode -> !startNodeKeys.contains(startNode.getId()))
                .toList();
        for (Node unJoinStartNode : unJoinStartNodes) {
            Flow subFlow = createSubFlow(flow, unJoinStartNode, startNodeOutgoers.get(unJoinStartNode.getId()));
            whenElList.add(convertEl(subFlow));
        }

        //有交集节点
        if (CollUtil.isNotEmpty(jointNodes)) {
            for (Map.Entry<Node, Set<String>> nodeSetEntry : jointNodes.entrySet()) {
                Set<String> currentNodeKeys = nodeSetEntry.getValue();
                List<Node> jointStartNodes = startNodes.stream()
                        .filter(node -> currentNodeKeys.contains(node.getId()))
                        .toList();
                Flow jointFlow = createJointFlow(flow, jointStartNodes, startNodeOutgoers, null);
                whenElList.add(convertEl(jointFlow));
            }
        }

        return whenEls(whenElList);
    }

    private static ELWrapper handleCommonNode(Flow flow,
                                              List<Node> startNodes,
                                              Map<String, List<Node>> startNodeOutgoers,
                                              Node commonNode) {
        Flow jointFlow = createJointFlow(
                flow,
                startNodes,
                startNodeOutgoers,
                commonNode
        );
        return convertEl(jointFlow);

    }

    public static ELWrapper convertEl(Flow flow) {
        List<Node> startNodes = flow.getStartNodes();
        List<ELWrapper> elWrappers = new ArrayList<>();
        while (CollUtil.isNotEmpty(startNodes)) {
            if (CollUtil.size(startNodes) > 1) {
                //如果当前节点有多个，则先找到他们的共同交点，切割子流程
                Map<String, List<Node>> startNodeOutgoers = getStartNodeOutgoers(flow, startNodes);
                // 查找所有起始节点的第一个共同交点
                Node commonNode = findCommonNode(startNodeOutgoers);
                //没有共同的交点则共同的交点是End节点
                if (Objects.isNull(commonNode)) {
                    elWrappers.add(handleNoCommonNode(flow, startNodes, startNodeOutgoers));
                    startNodes = null;
                } else {
                    elWrappers.add(handleCommonNode(flow, startNodes, startNodeOutgoers, commonNode));
                    startNodes = CollUtil.newArrayList(commonNode);
                }
            } else {
                Node first = CollUtil.getFirst(startNodes);
                elWrappers.add(processNode(flow, first));
                //特殊节点不处理
                startNodes = List.of(NodeType.IF, NodeType.LOOP_EACH_ITEM).contains(first.getType())
                        ? null
                        : flow.getOutgoers(first, false);

            }
        }
        return thenEls(elWrappers);
    }

    private static ELWrapper whenEls(List<ELWrapper> elWrappers) {
        return CollUtil.size(elWrappers) == 1
                ? elWrappers.get(0)
                : ELBus.when(elWrappers.toArray());
    }

    private static ELWrapper thenEls(List<ELWrapper> elWrappers) {
        return CollUtil.size(elWrappers) == 1
                ? elWrappers.get(0)
                : ELBus.then(elWrappers.toArray());
    }

    private static ELWrapper createIteratorEL(Node node, Flow flow) {
        Flow subFlow = loopSubProcess(node, flow);
        ELWrapper elWrapper = convertEl(subFlow);
        String completionCondition = node.getLoop().getCompletionCondition();
        if (StrUtil.isNotBlank(completionCondition)) {
            String completionConditionId = StrUtil.format("{}CompletionCondition", node.getId());
            String completionConditionDataId = StrUtil.format("{}CompletionConditionData", node.getId());
            elWrapper = ELBus.ifOpt(
                    ELBus.node(completionConditionId).data(completionConditionDataId, completionCondition),
                    elWrapper
            );
        }

        List<Connection> connections = getLoopDoneConnections(flow, subFlow.getId());
        IteratorELWrapper iteratorELWrapper = createIteratorEL(node).doOpt(elWrapper);
        List<Node> outgoers = flow.getOutgoers(subFlow.getId(), connections::contains);
        if (CollUtil.isNotEmpty(outgoers)) {
            String nextFlowId = "next_flow_" + node.getId();
            Flow nextFlow = createNextFlow(nextFlowId, outgoers, flow, node);
            return ELBus.then(iteratorELWrapper, convertEl(nextFlow));
        } else {
            return iteratorELWrapper;
        }
    }

    public static List<Connection> getLoopDoneConnections(Flow flow, String loopDoneSourcePoint) {
        if (CollUtil.isEmpty(flow.getConnections())) {
            return CollUtil.newArrayList();
        }
        return flow.getConnections().stream()
                .filter(connection -> Objects.equals(connection.getSource(), loopDoneSourcePoint)
                        && Objects.equals(PointType.LOOP_DONE, connection.getSourcePointType()))
                .toList();
    }

    private static IteratorELWrapper createIteratorEL(Node node) {
        String loopNodeId = StrUtil.format("{}Loop", node.getId());
        String loopNodeDataId = StrUtil.format("{}LoopData", node.getId());
        return ELBus.iteratorOpt(
                        ELBus.node(loopNodeId).data(loopNodeDataId, node.getLoop())
                )
                .parallel(!node.getLoop().getSequential());
    }

    private static void buildNode(Node node) {
        LiteFlowNodeBuilder.createCommonNode().setId(node.getId())
                .setName(node.getLabel())
                .setClazz(ServiceNodeComponent.class)
                .build();
        if (NodeType.IF == node.getType()) {
            LiteFlowNodeBuilder.createBooleanNode()
                    .setId(StrUtil.format("IF_{}", node.getId()))
                    .setName(StrUtil.format("IF_{}", node.getLabel()))
                    .setClazz(IFNodeComponent.class)
                    .build();
        }

        if (node.loopIsValid()) {
            LiteFlowNodeBuilder.createIteratorNode()
                    .setId(StrUtil.format("{}Loop", node.getId()))
                    .setName(StrUtil.format("{}Loop", node.getLabel()))
                    .setClazz(LoopNodeComponent.class)
                    .build();
            String completionCondition = node.getLoop().getCompletionCondition();
            if (StrUtil.isNotBlank(completionCondition)) {
                LiteFlowNodeBuilder.createBooleanNode()
                        .setId(StrUtil.format("{}CompletionCondition", node.getId()))
                        .setName(StrUtil.format("{}CompletionCondition", node.getLabel()))
                        .setClazz(IFNodeComponent.class)
                        .build();
            }
        }
    }

    private static IfELWrapper createIfNodeEl(Node node, Flow flow) {
        List<Connection> linkNextConnections = flow.getConnections().stream()
                .filter(connection -> Objects.equals(connection.getSource(), node.getId()))
                .toList();
        List<Connection> trueConnections = linkNextConnections.stream()
                .filter(connection -> Objects.equals(PointType.IF_TRUE, connection.getSourcePointType()))
                .toList();
        String trueFlowId = node.getId() + "trueFlow";
        List<Node> trueOutgoers = flow.getOutgoers(node.getId(), trueConnections::contains);
        Flow ifTrueFlow = new Flow();
        ifTrueFlow.setId(trueFlowId);
        ifTrueFlow.setName(trueFlowId);
        ifTrueFlow.setNodes(trueOutgoers);
        ifTrueFlow.setConnections(getOutgoerConnections(flow, node, trueOutgoers));
        List<Connection> falseConnections = linkNextConnections.stream()
                .filter(connection -> !trueConnections.contains(connection))
                .toList();
        List<Node> falseOutgoers = flow.getOutgoers(node.getId(), falseConnections::contains);
        String ifNodeId = StrUtil.format("IF_{}", node.getId());
        String ifNodeDataId = StrUtil.format("{}IfData", node.getId());
        String express = String.format("$.inputData.%s.result", node.getId());
        if (CollUtil.isEmpty(falseConnections)) {
            return ELBus.ifOpt(
                    ELBus.node(ifNodeId)
                            .data(ifNodeDataId, express),
                    convertEl(ifTrueFlow)
            );
        } else {
            String falseFlowId = node.getId() + "falseFlow";
            Flow ifFalseFlow = new Flow();
            ifFalseFlow.setId(falseFlowId);
            ifFalseFlow.setName(falseFlowId);
            ifFalseFlow.setNodes(falseOutgoers);
            ifFalseFlow.setConnections(falseConnections);
            return ELBus.ifOpt(
                    ELBus.node(ifNodeId)
                            .data(ifNodeDataId, express),
                    convertEl(ifTrueFlow),
                    convertEl(ifFalseFlow)
            );
        }

    }

    private static ELWrapper createServiceNodeEL(Flow flow, Node node) {
        ServiceData serviceData = new ServiceData();
        serviceData.setFlowId(flow.getId());
        serviceData.setNodeId(node.getId());
        serviceData.setServiceId(node.getServiceId());
        serviceData.setParameters(node.getData());
        String dataId = StrUtil.format("{}Data", node.getId());
        NodeELWrapper elWrapper = ELBus.node(node.getId()).data(StrUtil.format(dataId), JSONUtil.quote(JSONUtil.toJsonStr(serviceData), false));
        if (!node.loopIsValid()) {
            return elWrapper;
        }

        String completionCondition = node.getLoop().getCompletionCondition();
        ELWrapper wrapper = elWrapper;
        if (StrUtil.isNotBlank(completionCondition)) {
            String completionConditionId = StrUtil.format("{}CompletionCondition", node.getId());
            String completionConditionDataId = StrUtil.format("{}CompletionConditionData", node.getId());
            wrapper = ELBus.ifOpt(
                    ELBus.node(completionConditionId)
                            .data(completionConditionDataId, completionCondition),
                    elWrapper
            );
        }

        return createIteratorEL(node).doOpt(wrapper);

    }

    private static List<Connection> getOutgoerConnections(Flow flow, Node node, List<Node> outgoers) {
        List<String> nodeIds = outgoers.stream().map(Node::getId).toList();
        return flow.getConnections().stream()
                .filter(connection -> !Objects.equals(connection.getSource(), node.getId())
                        && (nodeIds.contains(connection.getSource()) && nodeIds.contains(connection.getTarget()))
                ).toList();
    }

    private static Flow loopSubProcess(Node node, Flow flow) {
        List<Connection> flowConnections = flow.getConnections();
        Map<String, Node> nodeMap = flow.getNodes().stream().collect(Collectors.toMap(Node::getId, n -> n));
        List<Node> loopEachNodes = new ArrayList<>();
        List<Connection> loopConnections = new ArrayList<>();
        if (CollUtil.isNotEmpty(flowConnections)) {
            List<Connection> connections = flowConnections.stream()
                    .filter(connection -> Objects.equals(node.getId(), connection.getSource())
                            && Objects.equals(PointType.LOOP_EACH, connection.getSourcePointType()))
                    .toList();
            loopConnections.addAll(connections);
            List<Node> loopEachItemNextNodes = connections.stream()
                    .map(connection -> nodeMap.get(connection.getTarget())).toList();
            loopEachNodes.addAll(loopEachItemNextNodes);
            for (Node loopEachItemNextNode : loopEachItemNextNodes) {
                loopEachNodes.addAll(flow.getOutgoers(loopEachItemNextNode));
                loopConnections.addAll(flow.getNodeConnections(loopEachItemNextNode));
            }
        }

        Node loopServiceNode = BeanUtil.copyProperties(node, Node.class);
        loopServiceNode.setLoop(null);
        loopServiceNode.setType(NodeType.SERVICE);
        loopEachNodes.add(0, loopServiceNode);
        String subFlowId = "loopProcess_" + node.getId();
        Flow subFlow = new Flow();
        subFlow.setId(subFlowId);
        subFlow.setNodes(loopEachNodes);
        subFlow.setConnections(loopConnections.stream().distinct().toList());
        if (CollUtil.isNotEmpty(flowConnections)) {
            //改变连线流转
            flowConnections.stream()
                    .filter(connection -> connection.getTarget().equals(node.getId()))
                    .forEach(connection -> connection.setTarget(subFlowId));
            flowConnections.stream().filter(
                            connection ->
                                    Objects.equals(connection.getSourcePointType(), PointType.LOOP_DONE)
                                            && connection.getSource().equals(node.getId())
                    )
                    .forEach(connection -> connection.setSource(subFlowId));
        }
        return subFlow;
    }

    public static <T extends NodeComponent> boolean getBooleanValue(T node) {
        String express = node.getCmpData(String.class);
        FlowExecutionContextImpl contextBean = node.getContextBean(FlowExecutionContextImpl.class);
        LoopItem currLoopObj = node.getCurrLoopObj();
        ExecutionContext executionContext = contextBean;
        if (Objects.nonNull(currLoopObj)) {
            executionContext = contextBean.getLoopContextMap().get(currLoopObj.getLoopKey());
        }
        Object value = executionContext.parseValue(express);
        return BooleanUtil.isTrue(BooleanUtil.toBooleanObject(Optional.ofNullable(value).orElse("").toString()));
    }
}
