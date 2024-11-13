package io.autoflow.flowable.utils;

import io.autoflow.core.model.Node;
import org.flowable.bpmn.model.ParallelGateway;

/**
 * 网关节点转换器
 *
 * @author yiuman
 * @date 2023/7/14
 */
public enum GatewayNodeConverter implements NodeConverter<ParallelGateway> {
    INSTANCE;

    @Override
    public ParallelGateway convert(Node node) {
        ParallelGateway parallelGateway = new ParallelGateway();
        parallelGateway.setId(node.getId());
        parallelGateway.setName(node.getLabel());
        return parallelGateway;
    }
}

