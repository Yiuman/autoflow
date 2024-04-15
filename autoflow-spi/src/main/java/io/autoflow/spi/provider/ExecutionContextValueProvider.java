package io.autoflow.spi.provider;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.ExecutionContext;

import java.util.Objects;

/**
 * 执行器上下文值提供器
 * 用于将上下文中的变量值/表达式解释后填充到对象
 *
 * @author yiuman
 * @date 2023/7/27
 */
public class ExecutionContextValueProvider extends BaseContextValueProvider {
    private final ExecutionContext executionContext;

    public ExecutionContextValueProvider(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public boolean containsKey(String key) {
        return executionContext.getParameters().containsKey(key)
                || executionContext.getVariables().containsKey(key)
                || executionContext.getInputData().containsKey(key);
    }

    @Override
    public Object get(Object key) {
        Object expressValue = getExpressValue(key);
        if (Objects.nonNull(expressValue)) {
            return expressValue;
        }
        if (Constants.INPUT_DATA.equals(key)) {
            return executionContext.getInputData();
        }
        return executionContext.getParameters()
                .getOrDefault(
                        key,
                        executionContext.getVariables().get(key)
                );
    }

    @Override
    public Object put(String name, Object object) {
        executionContext.getVariables().put(name, object);
        return object;
    }

    @Override
    public String toJsonStr() {
        JSONObject obj = JSONUtil.createObj();
        obj.putAll(executionContext.getParameters());
        obj.putAll(executionContext.getVariables());
        obj.set(Constants.INPUT_DATA, executionContext.getInputData());
        return JSONUtil.toJsonStr(obj);
    }
}
