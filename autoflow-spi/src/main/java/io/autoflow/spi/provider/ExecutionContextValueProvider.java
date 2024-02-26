package io.autoflow.spi.provider;

import cn.hutool.core.bean.copier.ValueProvider;
import cn.hutool.core.convert.Convert;
import com.googlecode.aviator.AviatorEvaluator;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.ExecutionContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 执行器上下文值提供器
 * 用于将上下文中的变量值/表达式解释后填充到对象
 *
 * @author yiuman
 * @date 2023/7/27
 */
public class ExecutionContextValueProvider implements ValueProvider<String> {
    private final Map<String, Object> variables = new HashMap<>();

    public ExecutionContextValueProvider(ExecutionContext executionContext) {
        variables.putAll(executionContext.getParameters());
        variables.put(Constants.INPUT_DATA, executionContext.getInputData());
    }

    @Override
    public Object value(String key, Type valueType) {
        Object result = variables.get(key);
        if (result instanceof String) {
            try {
                return AviatorEvaluator.execute((String) result, variables);
            } catch (Throwable ignore) {
            }
        }

        if (Objects.nonNull(result)) {
            result = Convert.convertWithCheck(valueType, result, null, true);
        }

        return result;
    }

    @Override
    public boolean containsKey(String key) {
        return variables.containsKey(key);
    }
}
