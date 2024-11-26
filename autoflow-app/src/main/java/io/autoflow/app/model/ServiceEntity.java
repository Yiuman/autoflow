package io.autoflow.app.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.autoflow.spi.model.Property;
import io.ola.crud.model.BaseEntity;
import io.ola.crud.utils.JsonbTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author yiuman
 * @date 2024/5/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("af_service")
public class ServiceEntity extends BaseEntity<String> {
    @Id
    private String id;
    private String name;
    private Boolean system;
    private String jarFileId;
    private String description;
    @Column(typeHandler = JsonbTypeHandler.class)
    private List<Property> properties;
    @Column(typeHandler = JsonbTypeHandler.class)
    private List<Property> outputType;
    private Boolean uninstall;
    @Transient
    private Map<String, Properties> i18n;
}
