package io.autoflow.flowable.utils;

import io.autoflow.core.model.Node;
import org.flowable.bpmn.model.FlowNode;

/**
 * @param <T> flowable节点类型
 * @author yiuman
 * @date 2023/7/14
 */
public interface NodeConverter<T extends FlowNode> {

    T convert(Node node);
}
