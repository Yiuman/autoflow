package io.autoflow.app.query;

import io.ola.crud.query.annotation.Like;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/7/17
 */
@Data
public class VariableQuery {
    @Like(mapping = "key")
    private String keyword;
}
