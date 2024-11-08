package io.autoflow.spi.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yiuman
 * @date 2024/11/8
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Select {
    String[] options();
}