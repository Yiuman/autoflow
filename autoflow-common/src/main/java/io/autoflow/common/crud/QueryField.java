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

    String method() default "EQUALS";

    String mapping() default "";

    Clauses clauses() default Clauses.AND;

    boolean require() default false;

    Class<? extends ConditionHandler> handler() default ConditionHandler.class;
}