package io.autoflow.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.autoflow.app.entity.FlowDefinition;

/**
 * @author yiuman
 * @date 2023/7/25
 */
public interface FlowDefinitionService extends IService<FlowDefinition> {

    /**
     * 流程部署
     *
     * @param flowDefinition 流程定义
     * @return 流程定义ID
     */
    String deploy(FlowDefinition flowDefinition);

}