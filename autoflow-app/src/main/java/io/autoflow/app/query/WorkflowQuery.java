package io.autoflow.app.query;

import io.ola.crud.query.annotation.In;
import io.ola.crud.query.annotation.Like;
import lombok.Data;

import java.util.Set;

/**
 * @author yiuman
 * @date 2024/5/14
 */
@Data
public class WorkflowQuery {
    @Like
    private String name;
    @In
    private Set<String> tagIds;
}
