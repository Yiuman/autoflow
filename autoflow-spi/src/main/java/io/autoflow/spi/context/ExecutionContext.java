package io.autoflow.spi.context;

import io.autoflow.spi.model.ExecutionData;

import java.util.Map;

/**
 * @author yiuman
 * @date 2023/7/11
 */
public interface ExecutionContext {

    Map<String, ExecutionData[]> getInputData();

    Map<String, Object> getParameters();

}