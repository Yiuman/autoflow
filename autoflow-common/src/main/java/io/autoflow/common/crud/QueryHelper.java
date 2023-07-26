package io.autoflow.common.crud;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yiuman
 * @date 2023/7/26
 */
public final class QueryHelper {

    private final static Map<Class<?>, List<QueryFieldMeta>> CLASS_QUERY_FIELD_META_MAP = new ConcurrentHashMap<>();

    private QueryHelper() {
    }

    public static Wrapper<?> build(Object any) {
        if (Objects.isNull(any)) {
            return Wrappers.emptyWrapper();
        }
        Class<?> objectClass = any.getClass();
        List<QueryFieldMeta> queryFieldMetas = getQueryFieldMetas(objectClass);
        if (CollUtil.isEmpty(queryFieldMetas)) {
            return Wrappers.emptyWrapper();
        }
        QueryWrapper<?> wrapper = Wrappers.query();
        for (QueryFieldMeta queryFieldMeta : queryFieldMetas) {
            Boolean require = queryFieldMeta.getRequire();
            Clauses clauses = queryFieldMeta.getClauses();
            //todo 处理不同类型的查询
            if (Clauses.AND == clauses) {
                wrapper.and(require, queryWrapper -> {
                });
            } else {
                wrapper.or(require, queryWrapper -> {
                });
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
