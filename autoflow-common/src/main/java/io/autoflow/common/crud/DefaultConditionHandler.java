package io.autoflow.common.crud;

import cn.hutool.core.util.ReflectUtil;
import com.mybatisflex.core.constant.SqlConsts;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.function.Consumer;

/**
 * @author yiuman
 * @date 2023/7/31
 */
public enum DefaultConditionHandler implements ConditionHandler {
    INSTANCE;

    @Override
    public Consumer<QueryWrapper> handle(QueryFieldMeta queryFieldMeta, Object conditionValue) {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setColumn(new QueryColumn(queryFieldMeta.getMapping()));
        queryCondition.setLogic((String) ReflectUtil.getStaticFieldValue(ReflectUtil.getField(SqlConsts.class, queryFieldMeta.getMethod())));
        queryCondition.setValue(conditionValue);
        return queryWrapper -> queryWrapper.where(queryCondition);
    }

}
