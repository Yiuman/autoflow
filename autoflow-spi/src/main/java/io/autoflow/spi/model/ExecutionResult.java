package io.autoflow.spi.model;

import cn.hutool.core.date.LocalDateTimeUtil;
import io.autoflow.spi.exception.InputValidateException;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * @param <DATA> 执行的结果数据
 * @author yiuman
 * @date 2024/8/2
 */
@Data
public class ExecutionResult<DATA> {
    private String flowId;
    private String nodeId;
    private String serviceId;
    private DATA data;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Error error;
    private Long durationMs;

    public static <T> ExecutionResult<T> error(ServiceData serviceData, Throwable throwable) {
        Error error = Error.builder().node(serviceData.getNodeId())
                .message(throwable.getMessage())
                .build();
        if (throwable instanceof InputValidateException) {
            error.setInputValidateErrors(((InputValidateException) throwable).getInputValidateErrors());
        }
        ExecutionResult<T> executionResult = new ExecutionResult<>();

        executionResult.setFlowId(serviceData.getFlowId());
        executionResult.setServiceId(serviceData.getServiceId());
        executionResult.setError(error);
        return executionResult;
    }

    public Long getDurationMs() {
        if (Objects.isNull(durationMs)
                && Objects.nonNull(startTime)
                && Objects.nonNull(endTime)) {
            durationMs = LocalDateTimeUtil.between(startTime, endTime, ChronoUnit.MILLIS);
        }

        return durationMs;
    }
}
