package io.autoflow.plugin.regex;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;

/**
 * 正则插件
 */
public class RegexService extends BaseService<RegexParameter> {
    @Override
    public String getName() {
        return "Regex";
    }

    @Override
    public ExecutionData execute(RegexParameter regexParameter) {
        Object result = regexParameter.getMethod().getFunc().apply(regexParameter);
        JSONObject jsonObject = JSONUtil.createObj();
        jsonObject.set(regexParameter.getMethod().name(), result);
        return ExecutionData.builder()
                .json(jsonObject)
                .build();
    }
}
