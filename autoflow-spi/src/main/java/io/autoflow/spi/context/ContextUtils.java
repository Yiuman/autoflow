package io.autoflow.spi.context;

import io.autoflow.spi.model.ExecutionResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yiuman
 * @date 2024/10/28
 */
public final class ContextUtils {

    public static final String GLOBAL_VARIABLE_NAME = "global";

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
                objects.add(executionResult.getData());
                inputData.put(executionResult.getNodeId(), objects);
            }
        } else if (Objects.nonNull(executionResult.getData())) {
            inputData.put(executionResult.getNodeId(), executionResult.getData());
        }

        Map<String, Object> variables = executionContext.getVariables();
        Map<String, Object> globalMap = (Map<String, Object>) variables.get(GLOBAL_VARIABLE_NAME);
        if (Objects.isNull(globalMap)) {
            synchronized (executionContext) {
                globalMap = new ConcurrentHashMap<>();
                executionContext.getVariables().put(GLOBAL_VARIABLE_NAME, globalMap);
            }
        }
        if (executionResult.getData() instanceof Map mapData) {
            globalMap.putAll(mapData);
        }


    }

}
