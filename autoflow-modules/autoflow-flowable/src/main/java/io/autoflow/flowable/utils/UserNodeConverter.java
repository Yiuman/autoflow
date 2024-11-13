package io.autoflow.flowable.utils;

import cn.hutool.core.lang.Assert;
import io.autoflow.core.model.Node;
import io.autoflow.core.model.NodeType;
import org.flowable.bpmn.model.UserTask;

/**
 * 用户任务节点转换器
 *
 * @author yiuman
 * @date 2023/7/14
 */
public enum UserNodeConverter implements NodeConverter<UserTask> {
    INSTANCE;

    @Override
    public UserTask convert(Node node) {
        Assert.equals(NodeType.USER, node.getType());
        UserTask userTask = new UserTask();
        userTask.setId(node.getId());
        return userTask;
    }

}

