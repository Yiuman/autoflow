package io.autoflow.spi.annotation;

import io.autoflow.spi.OptionValueProvider;
import io.autoflow.spi.enums.ComponentType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yiuman
 * @date 2024/11/8
 */

@Retention(RetentionPolicy.RUNTIME)
@Cmp(ComponentType.Select)
public @interface Select {
    String[] options() default {};

    String defaultValue() default "";

    Class<? extends OptionValueProvider> provider() default OptionValueProvider.class;

}