package io.autoflow.plugin.regex;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;

import java.util.List;

/**
 * 正则插件
 */
public class RegexService extends BaseService<RegexParameter> {
    @Override
    public String getName() {
        return "Regex";
    }

    @Override
    public List<ExecutionData> execute(RegexParameter regexParameter) {
        Object result = regexParameter.getMethod().getFunc().apply(regexParameter);
        JSONObject jsonObject = JSONUtil.createObj();
        jsonObject.set(regexParameter.getMethod().name(), result);
        return List.of(
                ExecutionData.builder()
                        .json(jsonObject)
                        .build()
        );
    }
}
