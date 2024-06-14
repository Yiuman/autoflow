package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yiuman
 * @date 2024/6/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("AF_FILE")
public class FileResource extends BaseEntity<String> {
    @Id
    private String id;
    private String filename;
    private Long size;
    private String path;
    private String metadata;
    private String platform;
    private String contentType;
}
