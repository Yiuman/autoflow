package io.autoflow.plugin.variableextract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.NamedValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            if (Objects.isNull(namedValue.getName())) {
                continue;
            }
            String key = StrUtil.toString(getParseValue(namedValue.getName(), executionContext));
            Object result = getParseValue(namedValue.getValue(), executionContext);
            extractValueMap.put(key, result);
        }
        return extractValueMap;
    }

    private Object getParseValue(Object value, ExecutionContext executionContext) {
        Object result = value;
        if (value instanceof String) {
            Object parseResult = executionContext.parseValue(StrUtil.toString(value));
            if (Objects.nonNull(parseResult)) {
                result = parseResult;
            }
        }
        return result;
    }
}
