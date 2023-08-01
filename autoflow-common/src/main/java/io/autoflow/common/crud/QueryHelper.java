package io.autoflow.common.crud;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ReflectUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.common.utils.SpringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author yiuman
 * @date 2023/7/26
 */
public final class QueryHelper {

    private final static Map<Class<?>, List<QueryFieldMeta>> CLASS_QUERY_FIELD_META_MAP = new ConcurrentHashMap<>();

    private final static ConditionHandler DEFAULT_CONDITION_HANDLER = DefaultConditionHandler.INSTANCE;

    private QueryHelper() {
    }

    public static QueryWrapper build(Object any) {
        if (Objects.isNull(any)) {
            return QueryWrapper.create();
        }
        Class<?> objectClass = any.getClass();
        List<QueryFieldMeta> queryFieldMetas = getQueryFieldMetas(objectClass);
        if (CollUtil.isEmpty(queryFieldMetas)) {
            return QueryWrapper.create();
        }
        QueryWrapper wrapper = QueryWrapper.create();
        for (QueryFieldMeta queryFieldMeta : queryFieldMetas) {
            boolean require = BooleanUtil.isTrue(queryFieldMeta.getRequire());
            Clauses clauses = queryFieldMeta.getClauses();
            Class<? extends ConditionHandler> handleClass = queryFieldMeta.getHandleClass();
            Consumer<QueryWrapper> queryWrapperConsumer;
            if (Objects.nonNull(handleClass)) {
                ConditionHandler conditionHandler = SpringUtils.getBean(handleClass, true);
                queryWrapperConsumer = conditionHandler.handle(queryFieldMeta, ReflectUtil.getFieldValue(any, queryFieldMeta.getField()));
            } else {
                queryWrapperConsumer = DEFAULT_CONDITION_HANDLER.handle(queryFieldMeta, ReflectUtil.getFieldValue(any, queryFieldMeta.getField()));
            }
            if (Clauses.AND == clauses) {
                wrapper.and(queryWrapperConsumer, require);
            } else {
                wrapper.or(queryWrapperConsumer, require);
            }

        }
        return wrapper;
    }

    public static List<QueryFieldMeta> getQueryFieldMetas(Class<?> queryClass) {
        synchronized (queryClass) {
            List<QueryFieldMeta> queryFieldMetas = CLASS_QUERY_FIELD_META_MAP.get(queryClass);
            if (Objects.nonNull(queryFieldMetas)) {
                return queryFieldMetas;
            }
            queryFieldMetas = new ArrayList<>();
            Field[] fieldsDirectly = ReflectUtil.getFieldsDirectly(queryClass, true);
            for (Field field : fieldsDirectly) {
                field.setAccessible(true);
                List<QueryFieldMeta> queryParamMeta = getQueryFieldMetas(queryClass, field);
                if (CollUtil.isNotEmpty(queryParamMeta)) {
                    queryFieldMetas.addAll(queryParamMeta);
                }
            }
            CLASS_QUERY_FIELD_META_MAP.put(queryClass, queryFieldMetas);
            return queryFieldMetas;
        }

    }

    private static List<QueryFieldMeta> getQueryFieldMetas(Class<?> queryClass, Field field) {
        Set<QueryField> allMergedAnnotations = AnnotatedElementUtils.getAllMergedAnnotations(field, QueryField.class);
        if (CollUtil.isEmpty(allMergedAnnotations)) {
            return null;
        }
        List<QueryFieldMeta> queryFieldMetas = new ArrayList<>();
        for (QueryField queryField : allMergedAnnotations) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(queryField);
            try {
                Field source = invocationHandler.getClass().getDeclaredField("annotation");
                source.setAccessible(true);
                MergedAnnotation<?> mergedAnnotation = (MergedAnnotation<?>) source.get(invocationHandler);
                Annotation sourceAnnotation = mergedAnnotation.getRoot().synthesize();
                queryFieldMetas.add(
                        new QueryFieldMeta(queryClass, field, queryField, sourceAnnotation)
                );
            } catch (IllegalAccessException | NoSuchFieldException ignore) {
            }

        }
        return queryFieldMetas;
    }
}
