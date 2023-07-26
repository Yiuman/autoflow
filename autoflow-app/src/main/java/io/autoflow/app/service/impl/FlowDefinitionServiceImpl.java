package io.autoflow.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.autoflow.app.dao.FlowDefinitionDao;
import io.autoflow.app.entity.FlowDefinition;
import io.autoflow.app.service.FlowDefinitionService;
import org.springframework.stereotype.Service;

/**
 * @author yiuman
 * @date 2023/7/26
 */
@Service
public class FlowDefinitionServiceImpl extends ServiceImpl<FlowDefinitionDao, FlowDefinition>
        implements FlowDefinitionService {

}
