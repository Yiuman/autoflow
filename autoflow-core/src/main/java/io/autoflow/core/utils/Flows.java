package io.autoflow.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.core.model.*;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程工具
 *
 * @author yiuman
 * @date 2023/7/13
 */
@SuppressWarnings("unchecked")
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
        put(NodeType.SWITCH, ServiceNodeConverter.INSTANCE);
        put(NodeType.GATEWAY, GatewayNodeConverter.INSTANCE);
        put(NodeType.USER, UserNodeConverter.INSTANCE);
        put(NodeType.SUBFLOW, SubProcessConverter.INSTANCE);
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
        return convert(JSONUtil.toBean(jsonStr, Flow.class));
    }

    /**
     * Flow实例转换Bpmn模型
     *
     * @param flow Flow流程定义实例
     * @return Bpmn模型
     */
    public static BpmnModel convert(Flow flow) {
        Assert.notNull(flow);
        Assert.notEmpty(flow.getNodes());
        Map<String, FlowElementsContainer> idFlowElementsContainerMap = new HashMap<>();
        //创建主流程
        Process process = createProcess(flow);
        idFlowElementsContainerMap.put(flow.getId(), process);
        StartEvent startEvent = createStartEvent();
        EndEvent endEvent = createEndEvent();
        process.addFlowElement(endEvent);
        process.addFlowElement(startEvent);

        //处理节点
        for (Node node : flow.getNodes()) {
            String dependentFlowId = flow.getDependentFlowId(node);
            FlowNode flowNode;
            //子流程特别处理
            if (NodeType.SUBFLOW == node.getType() && CollUtil.isEmpty(flow.getIncomers(node))
                    && CollUtil.isEmpty(flow.getOutgoers(node))) {
                flowNode = ServiceNodeConverter.INSTANCE.convert(node);
            } else {
                flowNode = NODE_CONVERTER_MAP.get(node.getType()).convert(node);
            }

            if (flowNode instanceof SubProcess subProcess) {
                idFlowElementsContainerMap.put(flowNode.getId(), subProcess);
            }
            FlowElementsContainer flowElementsContainer = idFlowElementsContainerMap.get(dependentFlowId);
            flowElementsContainer.addFlowElement(flowNode);
        }

        //处理连线
        List<Connection> connections = flow.getConnections();
        if (CollUtil.isNotEmpty(connections)) {
            for (Connection connection : flow.getConnections()) {
                String dependentFlowId = flow.getDependentFlowId(connection.getSource());
                FlowElementsContainer flowElementsContainer = idFlowElementsContainerMap.get(dependentFlowId);
                FlowElementsContainer currentSourceContainer = idFlowElementsContainerMap.get(connection.getSource());
                if (Objects.nonNull(currentSourceContainer) && currentSourceContainer instanceof SubProcess) {
                    continue;
                }
                flowElementsContainer.addFlowElement(createSequenceFlow(connection));
            }

        }

        //处理开始所有流程的开始与结束节点
        for (Map.Entry<String, FlowElementsContainer> idFlowElementsContainerEntry : idFlowElementsContainerMap.entrySet()) {
            String key = idFlowElementsContainerEntry.getKey();
            FlowElementsContainer flowElementsContainer = idFlowElementsContainerEntry.getValue();
            StartEvent startEventElement = getElement(flowElementsContainer, StartEvent.class);
            List<Node> startNodes = flow.getStartNodes(key);
            for (Node startNode : startNodes) {
                flowElementsContainer.addFlowElement(createSequenceFlow(new Connection(startEventElement.getId(), startNode.getId())));
            }

            EndEvent endEventElement = getElement(flowElementsContainer, EndEvent.class);
            List<Node> endNodes = flow.getEndNodes(key);
            for (Node endNode : endNodes) {
                flowElementsContainer.addFlowElement(createSequenceFlow(new Connection(endNode.getId(), endEventElement.getId())));
            }
        }

        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);
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
        //设置条件表达式
        if (StrUtil.isNotBlank(connection.getExpression())) {
            sequenceFlow.setConditionExpression(connection.getExpression());
        }
        sequenceFlow.setSourceRef(connection.getSource());
        sequenceFlow.setTargetRef(connection.getTarget());
        return sequenceFlow;
    }

    public static Process createProcess(Flow flow) {
        Process process = new Process();
        process.setId(flow.getId());
        process.setName(flow.getName());
        process.setDocumentation(flow.getDescription());
        addExtensionElement(process, AUTOFLOW_JSON, JSONUtil.toJsonStr(flow));
        return process;
    }

    public static StartEvent createStartEvent() {
        return createStartEvent(START_EVENT_ID);
    }

    public static StartEvent createStartEvent(String startEventId) {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(startEventId);
        return startEvent;
    }

    public static EndEvent createEndEvent() {
        return createEndEvent(END_EVENT_ID);
    }

    public static EndEvent createEndEvent(String endEventId) {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(endEventId);
        return endEvent;
    }

    public static <T> T getElement(FlowElementsContainer flowElementsContainer, Class<T> clazz) {
        return (T) flowElementsContainer.getFlowElements().stream()
                .filter(element -> clazz.isAssignableFrom(element.getClass()))
                .findFirst().orElse(null);
    }

    public static <T> List<T> getElements(FlowElementsContainer flowElementsContainer, Class<T> clazz) {
        return (List<T>) flowElementsContainer.getFlowElements().stream()
                .filter(element -> clazz.isAssignableFrom(element.getClass()))
                .collect(Collectors.toList());
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


    /**
     * 添加循环变量
     *
     * @param activity 活动
     * @param loop     循环配置参数
     */
    public static void addMultiInstanceLoopCharacteristics(Activity activity, Loop loop) {
        if (Objects.isNull(loop)
                || (Objects.isNull(loop.getLoopCardinality()) && StrUtil.isBlank(loop.getCollectionString()))) {
            return;
        }
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        multiInstanceLoopCharacteristics.setSequential(Optional.ofNullable(loop.getSequential()).orElse(false));
        multiInstanceLoopCharacteristics.setCompletionCondition(loop.getCompletionCondition());
        if (Objects.nonNull(loop.getLoopCardinality())) {
            multiInstanceLoopCharacteristics.setLoopCardinality(StrUtil.toString(loop.getLoopCardinality()));
        } else {
            multiInstanceLoopCharacteristics.setInputDataItem(String.format("${expressResolver.resolve(execution,'%s')}", loop.getCollectionString()));
            multiInstanceLoopCharacteristics.setElementVariable(loop.getElementVariable());
        }
        activity.setLoopCharacteristics(multiInstanceLoopCharacteristics);
    }

}
