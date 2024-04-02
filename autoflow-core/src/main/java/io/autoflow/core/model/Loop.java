package io.autoflow.core.model;

import lombok.Data;

/**
 * 循环参数
 *
 * @author yiuman
 * @date 2024/4/2
 */
@Data
public class Loop {
    /**
     * 固定值>0
     */
    private Integer loopCardinality;
    /**
     * 表达式取值（支持jsonpath/qlexpress）
     */
    private String collectionString;
    /**
     * 循环项的变量（当是loopCardinality时此值为loopCounter）
     */
    private String elementVariable = "loopCounter";
    /**
     * 是否串行
     */
    private Boolean sequential = false;
    /**
     * 完成条件（表达式）
     */
    private String completionCondition;
}
