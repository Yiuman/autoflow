package io.autoflow.plugin.loopeachitem;

import lombok.Data;

/**
 * @author yiuman
 * @date 2024/4/8
 */
@Data
public class LoopItem {
    private Integer loopCounter;
    /**
     * 循环项的变量（当是loopCardinality时此值为loopCounter）
     */
    private Object elementVariable;
    /**
     * 是否串行
     */
    private Boolean sequential = false;
    private Integer nrOfInstances;
}
