package io.autoflow.plugin.loopeachitem;

import lombok.Data;

/**
 * @author yiuman
 * @date 2024/4/8
 */
@Data
public class LoopItem {
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
    private String elementVariable;
    /**
     * 是否串行
     */
    private Boolean sequential = false;
    /**
     * 完成条件（表达式）
     */
    private String completionCondition;
    private String elementVariableValue;
    private Integer nrOfInstances;
}
