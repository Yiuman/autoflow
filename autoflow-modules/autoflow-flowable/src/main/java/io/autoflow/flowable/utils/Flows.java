package io.autoflow.flowable.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.core.enums.PointType;
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
        put(NodeType.IF, ServiceNodeConverter.INSTANCE);
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
        return convert(JSONUtil.toBean(jsonStr, Flow.class));
    }

    private static <T extends FlowElementsContainer> T createProcessObject(Class<T> processType, Flow flow) {
        T process = ReflectUtil.newInstance(processType);
        ReflectUtil.setFieldValue(process, "id", flow.getId());
        ReflectUtil.setFieldValue(process, "name", flow.getName());
        if (process instanceof Process mainProcess) {
            mainProcess.setDocumentation(flow.getDescription());
            addExtensionElement(mainProcess, AUTOFLOW_JSON, JSONUtil.toJsonStr(flow));
        }
        process.addFlowElement(createStartEvent(flow.getId() + START_EVENT_ID));
        process.addFlowElement(createEndEvent(flow.getId() + END_EVENT_ID));
        return process;
    }

    public static <T extends FlowElementsContainer> T createProcess(Class<T> processType, Flow flow) {
        Assert.notNull(flow);
        Assert.notEmpty(flow.getNodes());
        //创建流程
        T process = createProcessObject(processType, flow);
        Map<String, FlowElementsContainer> idFlowElementsContainerMap = new HashMap<>();
        idFlowElementsContainerMap.put(process.getId(), process);
        //创建节点 返回当前流程下的所有节点ID
        List<String> flowElementIds = createFlowElementsAndNodes(process, flow, idFlowElementsContainerMap);
        //处理连线
        createConnections(flow, flowElementIds, idFlowElementsContainerMap);
        processStartEndEvents(idFlowElementsContainerMap);
        return process;

    }

    private static List<String> createFlowElementsAndNodes(FlowElementsContainer process, Flow flow, Map<String, FlowElementsContainer> idFlowElementsContainerMap) {
        List<String> flowElementIds = new ArrayList<>();
        for (Node node : flow.getNodes()) {
            FlowNode flowNode;
            if (flowElementIds.contains(node.getId())) {
                continue;
            }
            if (NodeType.LOOP_EACH_ITEM == node.getType()) {
                flowNode = loopSubProcess(node, flow);
            } else {
                flowNode = NODE_CONVERTER_MAP.get(node.getType()).convert(node);
            }
            flowElementIds.add(flowNode.getId());
            if (flowNode instanceof FlowElementsContainer flowElementsContainer) {
                flowElementIds.addAll(flowElementsContainer
                        .getFlowElements()
                        .stream().map(BaseElement::getId)
                        .toList());
                idFlowElementsContainerMap.put(flowNode.getId(), flowElementsContainer);
            }
            process.addFlowElement(flowNode);
        }
        return flowElementIds;
    }

    private static void createConnections(Flow flow, List<String> flowElementIds, Map<String, FlowElementsContainer> idFlowElementsContainerMap) {
        List<Connection> connections = flow.getConnections();
        if (CollUtil.isNotEmpty(connections)) {
            for (Connection connection : connections) {
                String sequenceFlowId = String.format("%s_%s", connection.getSource(), connection.getTarget());
                if (flowElementIds.contains(sequenceFlowId)) {
                    continue;
                }
                String dependentFlowId = flow.getDependentFlowId(connection.getSource());
                FlowElementsContainer flowElementsContainer = idFlowElementsContainerMap.get(dependentFlowId);
                FlowElementsContainer currentSourceContainer = idFlowElementsContainerMap.get(connection.getSource());
                if (Objects.nonNull(currentSourceContainer) && currentSourceContainer instanceof SubProcess) {
                    continue;
                }
                flowElementsContainer.addFlowElement(createSequenceFlow(connection));
            }
        }
    }

    private static void processStartEndEvents(Map<String, FlowElementsContainer> idFlowElementsContainerMap) {
        for (Map.Entry<String, FlowElementsContainer> idFlowElementsContainerEntry : idFlowElementsContainerMap.entrySet()) {
            FlowElementsContainer flowElementsContainer = idFlowElementsContainerEntry.getValue();
            StartEvent startEventElement = getElement(flowElementsContainer, StartEvent.class);
            EndEvent endEventElement = getElement(flowElementsContainer, EndEvent.class);
            List<Activity> activities = getElements(flowElementsContainer, Activity.class);
            List<SequenceFlow> elements = getElements(flowElementsContainer, SequenceFlow.class);
            List<String> sourceRefs = elements.stream().map(SequenceFlow::getSourceRef).toList();
            List<String> targetRefs = elements.stream().map(SequenceFlow::getTargetRef).toList();
            for (Activity activity : activities) {
                if (!targetRefs.contains(activity.getId())) {
                    flowElementsContainer.addFlowElement(createSequenceFlow(new Connection(startEventElement.getId(), activity.getId())));
                }
                if (!sourceRefs.contains(activity.getId())) {
                    flowElementsContainer.addFlowElement(createSequenceFlow(new Connection(activity.getId(), endEventElement.getId())));
                }
            }
        }
    }

    public static SubProcess loopSubProcess(Node node, Flow flow) {
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
        SubProcess subProcess = createProcess(SubProcess.class, subFlow);
        Flows.addMultiInstanceLoopCharacteristics(subProcess, node.getLoop());
        return subProcess;
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
        BpmnModel bpmnModel = new BpmnModel();
        Process process = createProcess(Process.class, flow);
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

