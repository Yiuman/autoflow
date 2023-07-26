package io.autoflow.common.crud;

/**
 * 查询子句
 *
 * @author yiuman
 * @date 2021/8/16
 */
public enum Clauses {

    AND("AND"),

    OR("OR");

    private final String name;

    Clauses(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
