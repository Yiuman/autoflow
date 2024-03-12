package io.autoflow.spi.model;

import cn.hutool.json.JSON;
import io.autoflow.spi.exception.InputValidateException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionData {
    private JSON json;
    private String raw;
    private Binary binary;
    private Error error;
    private List<ExecutionData> batch;

    public static ExecutionData error(String serviceName, Throwable throwable) {
        Error error = Error.builder().node(serviceName)
                .message(throwable.getMessage())
                .build();
        if (throwable instanceof InputValidateException) {
            error.setInputValidateErrors(((InputValidateException) throwable).getInputValidateErrors());
        }
        return ExecutionData.builder()
                .error(error)
                .build();
    }
}