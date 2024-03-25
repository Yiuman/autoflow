package io.autoflow.app.request;

import io.autoflow.core.enums.ExecutionType;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/3/25
 */
@Data
public class StopRequest {
    private String id;
    private ExecutionType executionType;
}
