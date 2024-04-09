package io.autoflow.core.utils;

import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import lombok.Data;
import org.flowable.bpmn.model.FlowElement;

import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/9
 */
@Data
public class ConverterCtx {
    private final Flow flow;
    private Node node;
    private Map<String, Node> idNodeMap;
    private Map<String, List<Node>> incomerMap;
    private Map<String, List<Node>> outgoersMap;
    private List<FlowElement> flowElements;
}
