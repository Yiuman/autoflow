package io.autoflow.app.rest;

import io.autoflow.app.model.Workflow;
import io.ola.crud.rest.BaseQueryAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yiuman
 * @date 2024/4/28
 */
@RestController
@RequestMapping("/workflows")
public class WorkflowController implements BaseQueryAPI<Workflow> {

}
