package io.autoflow.common.crud;

import com.mybatisflex.core.query.QueryWrapper;

import java.util.function.Consumer;

/**
 * @author yiuman
 * @date 2023/7/31
 */
public interface ConditionHandler {

    Consumer<QueryWrapper> handle(QueryFieldMeta queryFieldMeta, Object conditionValue);
}