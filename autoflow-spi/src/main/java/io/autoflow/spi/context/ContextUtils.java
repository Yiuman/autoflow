package io.autoflow.spi.context;

import io.autoflow.spi.model.ExecutionResult;

import java.util.*;

/**
 * @author yiuman
 * @date 2024/10/28
 */
public final class ContextUtils {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <DATA> void addResult(ExecutionContext executionContext, ExecutionResult<DATA> executionResult) {
        Map<String, Object> inputData = executionContext.getInputData();
        //输入数据处理
        Object executionData = inputData.get(executionResult.getNodeId());
        if (Objects.nonNull(executionData)) {
            if (executionData instanceof Collection) {
                ((Collection) executionData).add(executionResult.getData());
            } else {
                List<Object> objects;
                synchronized (executionContext) {
                    objects = Collections.synchronizedList(new ArrayList<>());
                }
                objects.add(executionData);
                inputData.put(executionResult.getNodeId(), objects);
            }
        } else {
            inputData.put(executionResult.getNodeId(), executionResult.getData());
        }

    }
}
