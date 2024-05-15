package io.autoflow.app.query;

import io.ola.crud.query.annotation.Like;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/5/14
 */
@Data
public class WorkflowQuery {
    @Like
    private String name;
}
