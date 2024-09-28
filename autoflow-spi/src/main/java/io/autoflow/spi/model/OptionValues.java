package io.autoflow.spi.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yiuman
 * @date 2024/4/25
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface OptionValues {
    String[] value();

    OptionType type() default OptionType.STATIC;
}