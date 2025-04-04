package io.autoflow.spi.exception;

import cn.hutool.core.util.StrUtil;
import io.autoflow.spi.model.InputValidateError;
import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2024/3/12
 */
@Getter
public class InputValidateException extends RuntimeException {

    private final List<InputValidateError> inputValidateErrors;

    public InputValidateException(String message) {
        super(message);
        this.inputValidateErrors = null;
    }

    public <INPUT> InputValidateException(Set<ConstraintViolation<INPUT>> validated) {
        this.inputValidateErrors = validated.stream()
                .map(item -> new InputValidateError(item.getPropertyPath().toString(), item.getMessage()))
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (StrUtil.isBlank(message)) {
            return "Missing necessary parameters";
        }

        return message;
    }

}
