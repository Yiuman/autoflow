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
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.model.ServiceData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2024/4/11
 */
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
        return elWrapper.toEL(true);
    }

    public static ELWrapper convertEl(Flow flow) {
        if (CollUtil.isEmpty(flow.getNodes())) {
            return ELBus.node("empty");
        }
        List<Node> startNodes = flow.getStartNodes();
        List<ELWrapper> stepEl = new ArrayList<>();
        for (Node node : startNodes) {
            buildNode(node);
            if (NodeType.LOOP_EACH_ITEM == node.getType()) {
                stepEl.add(createIteratorEL(node, flow));
            } else if (NodeType.IF == node.getType()) {
                if (CollUtil.isEmpty(flow.getConnections())) {
                    stepEl.add(createServiceNodeEL(flow, node));
                } else {
                    stepEl.add(ELBus.then(createServiceNodeEL(flow, node), createIfNodeEl(node, flow)));
                }

            } else {
                List<Node> outgoers = flow.getOutgoers(node.getId());
                if (CollUtil.isNotEmpty(outgoers)) {
                    String nextFlowId = "next_flow_" + node.getId();
                    Flow nextFlow = new Flow();
                    nextFlow.setId(nextFlowId);
                    nextFlow.setNodes(outgoers);
                    nextFlow.setConnections(getOutgoerConnections(flow, node, outgoers));
                    stepEl.add(ELBus.then(createServiceNodeEL(flow, node), convertEl(nextFlow)));
                } else {
                    stepEl.add(createServiceNodeEL(flow, node));
                }

            }
        }

        return stepEl.size() == 1 ? CollUtil.getFirst(stepEl) : ELBus.when(stepEl.toArray());
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

        List<Connection> connections = flow.getConnections().stream()
                .filter(connection -> Objects.equals(connection.getSource(), subFlow.getId())
                        && Objects.equals(PointType.LOOP_DONE, connection.getSourcePointType()))
                .toList();

        IteratorELWrapper iteratorELWrapper = createIteratorEL(node).doOpt(elWrapper);
        List<Node> outgoers = flow.getOutgoers(subFlow.getId(), connections::contains);
        if (CollUtil.isNotEmpty(outgoers)) {
            String nextFlowId = "next_flow_" + node.getId();
            Flow nextFlow = new Flow();
            nextFlow.setId(nextFlowId);
            nextFlow.setNodes(outgoers);
            nextFlow.setConnections(getOutgoerConnections(flow, node, outgoers)
                    .stream().filter(connection -> !connections.contains(connection))
                    .toList());
            return ELBus.then(iteratorELWrapper, convertEl(nextFlow));
        } else {
            return iteratorELWrapper;
        }
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
        String falseFlowId = node.getId() + "falseFlow";
        Flow ifFalseFlow = new Flow();
        ifFalseFlow.setId(falseFlowId);
        ifFalseFlow.setName(falseFlowId);
        ifFalseFlow.setNodes(falseOutgoers);
        ifFalseFlow.setConnections(getOutgoerConnections(flow, node, falseOutgoers));
        String ifNodeId = StrUtil.format("IF_{}", node.getId());
        String ifNodeDataId = StrUtil.format("{}IfData", node.getId());
        String express = String.format("$.inputData.%s.[0].json.result", node.getId());
        return ELBus.ifOpt(
                ELBus.node(ifNodeId)
                        .data(ifNodeDataId, express),
                convertEl(ifTrueFlow),
                convertEl(ifFalseFlow)
        );
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
                    ELBus.node(completionConditionId).data(completionConditionDataId, completionCondition),
                    elWrapper
            );
        }

        return createIteratorEL(node).doOpt(wrapper);

    }

    private static List<Connection> getOutgoerConnections(Flow flow, Node node, List<Node> outgoers) {
        List<String> nodeIds = outgoers.stream().map(Node::getId).toList();
        return flow.getConnections().stream()
                .filter(connection -> !Objects.equals(connection.getSource(), node.getId())
                        && (nodeIds.contains(connection.getSource()) || nodeIds.contains(connection.getTarget()))
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
        Object value = node.getContextBean(FlowExecutionContext.class).parseValue(express);
        return BooleanUtil.isTrue(BooleanUtil.toBooleanObject(Optional.ofNullable(value).orElse("").toString()));
    }
}
