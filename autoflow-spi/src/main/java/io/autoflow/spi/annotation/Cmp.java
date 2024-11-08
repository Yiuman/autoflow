package io.autoflow.spi.annotation;

import io.autoflow.spi.enums.ComponentType;

import java.lang.annotation.*;

/**
 * @author yiuman
 * @date 2024/11/8
 */

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Cmp {
    ComponentType value();
}