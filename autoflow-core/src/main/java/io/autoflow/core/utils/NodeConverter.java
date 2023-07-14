package io.autoflow.core.utils;

import io.autoflow.core.model.Node;
import org.flowable.bpmn.model.FlowNode;

/**
 * @author yiuman
 * @date 2023/7/14
 */
public interface NodeConverter<T extends FlowNode> {

    T convert(Node node);
}