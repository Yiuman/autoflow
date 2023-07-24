package io.autoflow.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.autoflow.core.model.Connection;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.model.NodeType;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 流程工具
 *
 * @author yiuman
 * @date 2023/7/13
 */
public final class Flows {
    public static final String AUTOFLOW_JSON = "autoflow-json";
    public static final String PREFIX = "flowable";
    public static final String NAMESPACE = "http://flowable.com/modeler";
    public static final String START_EVENT_ID = "startEventNode";
    public static final String END_EVENT_ID = "endEventNode";
    public static final String PROPERTIES_EL_NAME = "properties";
    public static final String PROPERTY_EL_NAME = "property";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_VALUE = "value";

    private static final Map<NodeType, NodeConverter<? extends FlowNode>> NODE_CONVERTER_MAP = new HashMap<>() {{
        put(NodeType.SERVICE, ServiceNodeConverter.INSTANCE);
        put(NodeType.GATEWAY, GatewayNodeConverter.INSTANCE);
        put(NodeType.USER, UserNodeConverter.INSTANCE);
    }};

    private Flows() {
    }

    /**
     * Flow实例的JSON字符串转成Bpmn模型
     *
     * @param jsonStr Flow实例的JSON字符串
     * @return Bpmn模型
     */
    public static BpmnModel convert(String jsonStr) {
        return convert(JSON.parseObject(jsonStr, Flow.class));
    }

    /**
     * Flow实例转换Bpmn模型
     *
     * @param flow Flow流程定义实例
     * @return Bpmn模型
     */
    public static BpmnModel convert(Flow flow) {
        BpmnModel bpmnModel = new BpmnModel();
        Process process = createProcess(flow);
        List<Node> nodes = flow.getNodes();
        StartEvent startEvent = createStartEvent();
        process.addFlowElement(startEvent);
        flow.getStartNodes().forEach(startNode -> process.addFlowElement(
                createSequenceFlow(new Connection(startEvent.getId(), startNode.getId()))
        ));
        nodes.forEach(node -> process.addFlowElement(NODE_CONVERTER_MAP.get(node.getType()).convert(node)));
        flow.getConnections().forEach(connection -> process.addFlowElement(createSequenceFlow(connection)));
        EndEvent endEvent = createEndEvent();
        process.addFlowElement(endEvent);
        flow.getEndNodes().forEach(endNode -> process.addFlowElement(
                createSequenceFlow(new Connection(endNode.getId(), endEvent.getId()))
        ));
        autoLayout(bpmnModel);
        return bpmnModel;
    }

    /**
     * Bpmn模型自动排版
     *
     * @param bpmnModel Bpmn模型
     */
    public static void autoLayout(BpmnModel bpmnModel) {
        new BpmnAutoLayout(bpmnModel).execute();
    }

    /**
     * 创建bpmn连线
     *
     * @param connection Flow的Connection连线对象
     * @return bpmn的SequenceFlow连线对象
     */
    public static SequenceFlow createSequenceFlow(Connection connection) {
        SequenceFlow sequenceFlow = new SequenceFlow();
        String id = String.format("%s_%s", connection.getSource(), connection.getTarget());
        sequenceFlow.setId(id);
        sequenceFlow.setName(id);
        sequenceFlow.setSourceRef(connection.getSource());
        sequenceFlow.setTargetRef(connection.getTarget());
        return sequenceFlow;
    }

    public static Process createProcess(Flow flow) {
        Process process = new Process();
        process.setId(flow.getId());
        process.setName(flow.getName());
        process.setDocumentation(flow.getDescription());
        addExtensionElement(process, AUTOFLOW_JSON, JSON.toJSONString(flow));
        return process;
    }

    public static StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(START_EVENT_ID);
        return startEvent;
    }

    public static EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(END_EVENT_ID);
        return endEvent;
    }

    /**
     * 添加扩展属性
     *
     * @param flowElement 需要添加扩展属性的节点
     * @param name        属性名
     * @param value       属性值
     * @param <T>         节点类型
     */
    public static <T extends BaseElement> void addExtensionElement(T flowElement, String name, Object value) {
        if (StrUtil.isBlank(name) || Objects.isNull(value)) {
            return;
        }
        ExtensionElement propertiesElement;
        boolean isNew = false;
        List<ExtensionElement> extensionElementList = flowElement.getExtensionElements().get(PROPERTIES_EL_NAME);
        if (CollUtil.isNotEmpty(extensionElementList)) {
            propertiesElement = extensionElementList.get(0);
        } else {
            isNew = true;
            propertiesElement = new ExtensionElement();
            propertiesElement.setNamespace(NAMESPACE);
            propertiesElement.setNamespacePrefix(PREFIX);
            propertiesElement.setName(PROPERTIES_EL_NAME);
        }

        ExtensionElement childElements = new ExtensionElement();
        childElements.setNamespace(NAMESPACE);
        childElements.setNamespacePrefix(PREFIX);
        childElements.setName(PROPERTY_EL_NAME);

        ExtensionAttribute nameAttribute = new ExtensionAttribute();
        nameAttribute.setName(PROPERTY_NAME);
        nameAttribute.setValue(name);
        childElements.addAttribute(nameAttribute);

        ExtensionAttribute valueAttribute = new ExtensionAttribute();
        valueAttribute.setName(PROPERTY_VALUE);
        valueAttribute.setValue(value.toString());
        childElements.addAttribute(valueAttribute);

        propertiesElement.addChildElement(childElements);

        if (isNew) {
            flowElement.addExtensionElement(propertiesElement);
        }

    }

    /**
     * 获取流程节点的扩展属
     *
     * @param flowElement 流程节点
     * @return Key为属性名, value属性值的Map对象
     */
    public static Map<String, Object> getElementProperties(FlowElement flowElement) {
        Map<String, Object> elementProperties = new HashMap<>();
        Map<String, List<ExtensionElement>> extensionElements = flowElement.getExtensionElements();
        if (MapUtil.isEmpty(extensionElements)) {
            return elementProperties;
        }
        List<ExtensionElement> property = extensionElements.get(PROPERTIES_EL_NAME)
                .stream()
                .map(item -> item.getChildElements().get(PROPERTY_EL_NAME))
                .reduce(CollUtil.newArrayList(), (collect, current) -> {
                    if (CollUtil.isEmpty(current)) {
                        return collect;
                    }
                    collect.addAll(current);
                    return collect;
                });
        property.forEach(item -> elementProperties.put(
                item.getAttributeValue(null, PROPERTY_NAME),
                item.getAttributeValue(null, PROPERTY_VALUE)
        ));
        return elementProperties;
    }


}
