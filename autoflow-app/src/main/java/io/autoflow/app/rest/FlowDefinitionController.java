package io.autoflow.app.rest;

import io.autoflow.app.entity.FlowDefinition;
import io.autoflow.common.crud.BaseRESTAPI;
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
}
