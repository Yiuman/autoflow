package io.autoflow.spi.context;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import io.autoflow.spi.model.ExecutionData;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 单个执行的上下文
 *
 * @author yiuman
 * @date 2023/7/13
 */
@Data
public final class OnceExecutionContext implements ExecutionContext {
    private final Map<String, Object> parameters = new HashMap<>() {{
        put(Constants.INPUT_NAME, Constants.DEFAULT_INPUT_NAME);
        put(Constants.INPUT_INDEX, Constants.DEFAULT_INPUT_INDEX);
    }};
    private Map<String, List<ExecutionData>> inputData = new HashMap<>();

    public static OnceExecutionContext create(List<ExecutionData> executionData) {
        OnceExecutionContext onceExecutionContext = new OnceExecutionContext();
        onceExecutionContext.getInputData().put(Constants.DEFAULT_INPUT_NAME, executionData);
        return onceExecutionContext;
    }

    public static OnceExecutionContext create(ExecutionData executionData) {
        return create(CollUtil.newArrayList(executionData));
    }

    public static OnceExecutionContext create(Map<String, Object> parameters) {
        return create(parameters, null);
    }

    public static OnceExecutionContext create(Map<String, Object> parameters, Map<String, List<ExecutionData>> inputData) {
        OnceExecutionContext onceExecutionContext = new OnceExecutionContext();
        if (Objects.nonNull(parameters)) {
            onceExecutionContext.getParameters().putAll(parameters);
        }

        if (Objects.nonNull(inputData)) {
            onceExecutionContext.getInputData().putAll(inputData);
        }

        return onceExecutionContext;
    }

    public static <T> OnceExecutionContext create(T parameters) {
        return create(BeanUtil.beanToMap(parameters));
    }
}
