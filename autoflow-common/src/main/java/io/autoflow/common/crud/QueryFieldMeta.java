package io.autoflow.common.crud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryFieldMeta {
    private Class<?> objectClass;
    private Field field;
    private Annotation annotation;
    private String method;
    private Clauses clauses;
    private Boolean require;
    private String mapping;
    private List<QueryFieldMeta> nested;

    public QueryFieldMeta(Class<?> objectClass, Field field, QueryField queryField, Annotation sourceAnnotation) {
        this.objectClass = objectClass;
        this.field = field;
        this.annotation = sourceAnnotation;
        this.method = queryField.method();
        this.clauses = queryField.clauses();
        this.require = queryField.require();
        this.mapping = queryField.mapping();
    }
}
