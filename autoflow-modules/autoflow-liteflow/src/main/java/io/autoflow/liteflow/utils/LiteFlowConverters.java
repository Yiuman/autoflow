package io.autoflow.liteflow.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.builder.LiteFlowNodeBuilder;
import com.yomahub.liteflow.builder.el.ELBus;
import io.autoflow.core.model.*;
import io.autoflow.liteflow.cmp.IFNodeComponent;
import io.autoflow.liteflow.cmp.ServiceNodeComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2024/4/11
 */
public final class LiteFlowConverters {
    private LiteFlowConverters() {
    }

    public static String convertEl(Flow flow) {
        if (CollUtil.isEmpty(flow.getNodes())) {
            return "empty";
        }
        List<Node> startNodes = flow.getStartNodes();
        List<String> stepEl = new ArrayList<>();
        for (Node node : startNodes) {
            LiteFlowNodeBuilder.createCommonNode().setId(node.getId())
                    .setName(node.getLabel())
                    .setClazz(ServiceNodeComponent.class)
                    .build();
            if (NodeType.SWITCH == node.getType()) {
                LiteFlowNodeBuilder.createIfNode()
                        .setId(StrUtil.format("IF_{}", node.getId()))
                        .setName(StrUtil.format("IF_{}", node.getLabel()))
                        .setClazz(IFNodeComponent.class)
                        .build();
            }

            if (NodeType.LOOP_EACH_ITEM == node.getType()) {
                Flow subFlow = loopSubProcess(node, flow);
                Loop loop = node.getLoop();
                stepEl.add(ELBus.forOpt(loop.getCollectionString())
                        .parallel(true)
                        .doOpt(convertEl(subFlow))
                        .breakOpt(loop.getCompletionCondition())
                        .toEL());
            } else if (NodeType.SWITCH == node.getType()) {
                List<Connection> linkNextConnections = flow.getConnections().stream()
                        .filter(connection -> Objects.equals(connection.getSource(), node.getId()))
                        .toList();
                List<Connection> trueConnections = linkNextConnections.stream()
                        .filter(connection -> connection.getSourcePointType().equals("IF_TRUE"))
                        .toList();
                String trueFlowId = "if_true_flow_" + node.getId();
                List<Node> trueOutgoers = flow.getOutgoers(node.getId(), trueConnections::contains);
                Flow ifTrueFlow = new Flow();
                ifTrueFlow.setId(trueFlowId);
                ifTrueFlow.setName(trueFlowId);
                ifTrueFlow.setNodes(trueOutgoers);
                ifTrueFlow.setConnections(getOutgoerConnections(flow, node, trueOutgoers));

                List<Connection> falseConnections = linkNextConnections.stream().filter(connection -> !trueConnections.contains(connection))
                        .toList();
                List<Node> falseOutgoers = flow.getOutgoers(node.getId(), falseConnections::contains);
                String falseFlowId = "if_false_flow_" + node.getId();
                Flow ifFalseFlow = new Flow();
                ifFalseFlow.setId(falseFlowId);
                ifFalseFlow.setName(falseFlowId);
                ifFalseFlow.setNodes(falseOutgoers);
                ifFalseFlow.setConnections(getOutgoerConnections(flow, node, falseOutgoers));
                stepEl.add(ELBus
                        .ifOpt(StrUtil.format("IF_{}", node.getId()), convertEl(ifTrueFlow), convertEl(ifFalseFlow))
                        .toEL());
            } else {
                List<Node> outgoers = flow.getOutgoers(node.getId());
                if (CollUtil.isNotEmpty(outgoers)) {
                    String nextFlowId = "next_flow_" + node.getId();
                    Flow nextFlow = new Flow();
                    nextFlow.setId(nextFlowId);
                    nextFlow.setNodes(outgoers);
                    nextFlow.setConnections(getOutgoerConnections(flow, node, outgoers));
                    stepEl.add(convertEl(nextFlow));
                } else {
                    stepEl.add(node.getId());
                }

            }
        }
        return ELBus.when(stepEl.toArray()).toEL(true);
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
                            && Objects.equals("loop_each_item_loop_handle", connection.getSourcePointType()))
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
                                    Objects.equals(connection.getSourcePointType(), "loop_each_item_done_handle")
                                            && connection.getSource().equals(node.getId())
                    )
                    .forEach(connection -> connection.setSource(subFlowId));
        }
        return subFlow;
    }
}
