package io.autoflow.spi.annotation;

import io.autoflow.spi.enums.ComponentType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yiuman
 * @date 2024/11/8
 */

@Retention(RetentionPolicy.RUNTIME)
@Cmp(ComponentType.Code)
public @interface Code {
    String lang();
}