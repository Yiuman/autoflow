package io.autoflow.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import io.autoflow.core.Services;
import io.autoflow.core.delegate.ExecuteServiceTask;
import io.autoflow.core.model.Node;
import io.autoflow.core.model.NodeType;
import io.autoflow.spi.Service;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.ServiceTask;

import java.util.Map;

/**
 * 服务节点转换器
 * 将节点定义的插件信息转成服务任务
 * 使用io.autoflow.core.delegate.ExecuteServiceTask驱动
 *
 * @author yiuman
 * @date 2023/7/14
 */
public enum ServiceNodeConverter implements NodeConverter<ServiceTask> {
    INSTANCE;

    @Override
    public ServiceTask convert(Node node) {
        Assert.equals(NodeType.SERVICE, node.getType());
        Service service = Services.getServiceMap().get(node.getServiceName());
        Assert.notNull(service);
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setId(node.getId());
        serviceTask.setName(node.getName());
        serviceTask.setImplementation(ExecuteServiceTask.class.getName());
        serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        //字段属性注入
        FieldExtension fieldExtension = new FieldExtension();
        fieldExtension.setFieldName("serviceName");
        fieldExtension.setStringValue(service.getName());
        serviceTask.setFieldExtensions(CollUtil.newArrayList(fieldExtension));
        //扩展属性注入
        Map<String, Object> parameters = node.getParameters();
        if (MapUtil.isNotEmpty(parameters)) {
            parameters.forEach((key, value) -> Flows.addExtensionElement(serviceTask, key, value));
        }

        return serviceTask;
    }
}
