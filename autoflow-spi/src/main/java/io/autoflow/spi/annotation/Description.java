package io.autoflow.spi.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yiuman
 * @date 2024/8/15
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
    String value();
}