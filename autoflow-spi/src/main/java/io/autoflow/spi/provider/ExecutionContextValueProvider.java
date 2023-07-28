package io.autoflow.spi.provider;

import cn.hutool.core.bean.copier.ValueProvider;
import cn.hutool.core.convert.Convert;
import com.googlecode.aviator.AviatorEvaluator;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.ExecutionContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
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

        if (null != result) {
            // 尝试将结果转换为目标类型，如果转换失败，返回null，即跳过此属性值。
            // 来自：issues#I41WKP@Gitee，当忽略错误情况下，目标类型转换失败应返回null
            // 如果返回原值，在集合注入时会成功，但是集合取值时会报类型转换错误
            result = Convert.convertWithCheck(valueType, result, null, true);
        }

        return result;
    }

    @Override
    public boolean containsKey(String key) {
        return variables.containsKey(key);
    }
}
