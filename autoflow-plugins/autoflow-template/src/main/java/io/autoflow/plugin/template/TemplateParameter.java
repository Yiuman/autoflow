package io.autoflow.plugin.template;

import io.autoflow.spi.annotation.Code;
import io.autoflow.spi.model.NamedValue;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/11/29
 */
@Data
public class TemplateParameter {
    private List<NamedValue<Object>> attrs;
    @Code(lang = "xml")
    private String template;
}
