package io.autoflow.app.rest;

import com.baomidou.mybatisplus.extension.service.IService;
import io.autoflow.app.entity.FlowDefinition;
import io.autoflow.app.service.FlowDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yiuman
 * @date 2023/7/25
 */
@RestController
@RequestMapping("/flows")
@RequiredArgsConstructor
public class FlowDefinitionController implements BaseRESTAPI<FlowDefinition> {

    private final FlowDefinitionService flowDefinitionService;

    @Override
    public IService<FlowDefinition> getService() {
        return flowDefinitionService;
    }
}
