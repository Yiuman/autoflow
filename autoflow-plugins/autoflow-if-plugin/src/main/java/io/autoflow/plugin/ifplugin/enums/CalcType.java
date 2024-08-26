package io.autoflow.plugin.ifplugin.enums;

/**
 * @author yiuman
 * @date 2024/4/16
 */
public enum CalcType {
    Express("Express"),
    Equal("=="),
    NotEqual("!="),
    LT("<"),
    GT(">"),
    LTE("<="),
    GTE(">="),
    Like("LIKE"),
    In("IN"),
    Empty("Empty"),
    NotEmpty("NotEmpty");

    private final String symbol;

    CalcType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
