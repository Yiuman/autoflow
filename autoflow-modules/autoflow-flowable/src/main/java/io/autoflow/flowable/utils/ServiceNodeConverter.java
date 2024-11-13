package io.autoflow.flowable.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import io.autoflow.core.Services;
import io.autoflow.core.model.Node;
import io.autoflow.flowable.delegate.ExecuteServiceTask;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.Constants;
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
        Service service = Services.getServiceMap().get(node.getServiceId());
        Assert.notNull(service);
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setId(node.getId());
        serviceTask.setName(node.getLabel());
        serviceTask.setImplementation(ExecuteServiceTask.class.getName());
        serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        //字段属性注入
        FieldExtension fieldExtension = new FieldExtension();
        fieldExtension.setFieldName("serviceId");
        fieldExtension.setStringValue(service.getId());
        serviceTask.setFieldExtensions(CollUtil.newArrayList(fieldExtension));
        //扩展属性注入
        Map<String, Object> parameters = node.getData();
        if (MapUtil.isNotEmpty(parameters)) {
            for (Map.Entry<String, Object> stringObjectEntry : parameters.entrySet()) {
                //输入的data不作为参数字段输入
                if (Constants.INPUT_DATA.equals(stringObjectEntry.getKey())) {
                    continue;
                }
                Flows.addExtensionElement(serviceTask, stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
        }
        //添加循环参数
        Flows.addMultiInstanceLoopCharacteristics(serviceTask, node.getLoop());
        return serviceTask;
    }
}

