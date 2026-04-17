package io.autoflow.app.query;

import io.ola.crud.query.annotation.In;
import io.ola.crud.query.annotation.Like;
import lombok.Data;

import java.util.Set;

/**
 * Query for AgentConfig.
 *
 * @author yiuman
 * @date 2024/12/19
 */
@Data
public class AgentConfigQuery {

    @In(mapping = "id")
    private Set<String> ids;

    @Like
    private String name;
}
