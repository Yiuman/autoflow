package io.autoflow.plugin.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import io.autoflow.spi.utils.PropertyUtils;

/**
 * @author yiuman
 * @date 2024/11/29
 */
public class TemplateRender extends BaseService<TemplateParameter, TemplateResult> {

    private static final TemplateEngine TEMPLATE_ENGINE = TemplateUtil.createEngine(new TemplateConfig());

    @Override
    public String getName() {
        return "TemplateRenderer";
    }

    @Override
    public TemplateResult execute(TemplateParameter templateParameter, ExecutionContext executionContext) {
        Template template = TEMPLATE_ENGINE.getTemplate(templateParameter.getTemplate());
        return new TemplateResult(
                template.render(PropertyUtils.nameValuesToMap(templateParameter.getAttrs()))
        );
    }

    @Override
    protected TemplateParameter buildInput(ExecutionContext executionContext) {
        TemplateParameter templateParameter = new TemplateParameter();
        BeanUtil.fillBean(
                templateParameter,
                new ExecutionContextValueProvider(executionContext),
                CopyOptions.create()
                        .setIgnoreProperties("template")
        );
        templateParameter.setTemplate((String) executionContext.getParameters().get("template"));
        return templateParameter;
    }
}
