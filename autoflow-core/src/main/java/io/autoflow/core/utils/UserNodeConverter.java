package io.autoflow.core.utils;

import cn.hutool.core.lang.Assert;
import io.autoflow.core.model.Node;
import io.autoflow.core.model.NodeType;
import org.flowable.bpmn.model.UserTask;

/**
 * @author yiuman
 * @date 2023/7/14
 */
public enum UserNodeConverter implements NodeConverter<UserTask> {
    INSTANCE;

    @Override
    public UserTask convert(Node node) {
        Assert.equals(NodeType.USER, node.getType());
        return new UserTask();
    }

}
