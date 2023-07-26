package io.autoflow.common.crud;

import java.lang.annotation.*;

/**
 * @author yiuman
 * @date 2023/7/26
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface QueryField {

    String method() default "eq";

    String mapping() default "";

    Clauses clauses() default Clauses.AND;

    boolean require() default false;
}