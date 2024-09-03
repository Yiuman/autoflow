package io.autoflow.plugin.variableextract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.common.utils.NamedValue;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/9/3
 */
public class VariableExtractService extends BaseService<VariableExtractParameter, Map<String, Object>> {

    @Override
    public String getName() {
        return "VariableExtract";
    }

    @Override
    public Map<String, Object> execute(VariableExtractParameter variableExtractParameter, ExecutionContext executionContext) {
        List<NamedValue<Object>> namedValues = variableExtractParameter.getAttrs();
        if (CollUtil.isEmpty(namedValues)) {
            return MapUtil.empty();
        }
        Map<String, Object> extractValueMap = new HashMap<>(namedValues.size());
        for (NamedValue<Object> namedValue : namedValues) {
            Object value = namedValue.getValue();
            Object result = value;
            if (value instanceof String) {
                result = executionContext.parseValue(StrUtil.toString(value));
            }
            extractValueMap.put(namedValue.getName(), result);
        }
        return extractValueMap;
    }
}
