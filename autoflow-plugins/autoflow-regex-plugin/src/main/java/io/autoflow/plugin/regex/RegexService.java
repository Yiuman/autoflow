package io.autoflow.plugin.regex;

import cn.hutool.core.util.ReflectUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;

/**
 * 正则插件
 */
public class RegexService extends BaseService<RegexParameter, RegexResult> {
    @Override
    public String getName() {
        return "Regex";
    }

    @Override
    public RegexResult execute(RegexParameter regexParameter, ExecutionContext ctx) {
        RegexResult regexResult = new RegexResult();
        RegexMethod method = regexParameter.getMethod();
        ReflectUtil.setFieldValue(regexResult, method.name(), method.getFunc().apply(regexParameter));
        return regexResult;
    }
}
