package io.autoflow.app.query;

import io.ola.crud.query.annotation.In;
import io.ola.crud.query.annotation.Like;
import lombok.Data;

import java.util.Set;

@Data
public class ModelQuery {
    @In(mapping = "id")
    private Set<String> ids;
    @Like
    private String name;
}
