package io.autoflow.app.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.autoflow.spi.model.Property;
import io.ola.crud.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author yiuman
 * @date 2024/5/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("AF_SERVICE")
public class ServiceEntity extends BaseEntity<String> {
    @Id
    private String id;
    private String name;
    private Boolean system;
    private String jarFileId;
    private String description;
    private List<Property> properties;
    private List<Property> outputType;
    private Boolean uninstall;
    private transient Map<String, Properties> i18n;
}
