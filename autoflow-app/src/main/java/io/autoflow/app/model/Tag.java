package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiuman
 * @date 2024/5/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("AF_TAG")
public class Tag extends BaseEntity<String> {
    @Id
    private String id;
    private String name;
}
