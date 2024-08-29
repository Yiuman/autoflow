package io.autoflow.spi.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/8/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FlowExecutionResult extends ExecutionResult<List<ExecutionResult<Object>>> {
}
