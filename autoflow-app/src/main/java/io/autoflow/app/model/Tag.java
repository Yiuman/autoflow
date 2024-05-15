package io.autoflow.app.model;

import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiuman
 * @date 2024/5/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Tag extends BaseEntity<String> {
    private String id;
    private String name;
}
