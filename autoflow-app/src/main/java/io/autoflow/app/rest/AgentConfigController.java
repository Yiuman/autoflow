package io.autoflow.app.rest;

import io.autoflow.app.model.AgentConfig;
import io.autoflow.app.query.AgentConfigQuery;
import io.ola.crud.query.annotation.Query;
import io.ola.crud.rest.BaseRESTAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for AgentConfig.
 *
 * @author yiuman
 * @date 2024/12/19
 */
@RequestMapping("/agent-configs")
@RestController
@Query(AgentConfigQuery.class)
public class AgentConfigController implements BaseRESTAPI<AgentConfig> {
}
