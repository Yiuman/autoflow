package io.autoflow.spi.context;

import java.util.Map;

/**
 * @author yiuman
 * @date 2023/7/11
 */
public interface ExecutionContext {

    /**
     * 执行上下文的参数信息（节点入参）
     */
    Map<String, Object> getParameters();

    /**
     * 执行上下文的输入信息 （前节点的输出数据）
     * key为节点的ID，value是节点的输出值（若多次执行值为集合）
     */
    Map<String, Object> getInputData();

    /**
     * 执行过程中的变量信息
     */
    Map<String, Object> getVariables();


    /**
     * 根据KEY值解析值
     *
     * @param key 数据字段/表达式
     * @return 值
     */
    Object parseValue(String key);

    default ExecutionContext getParent() {
        return null;
    }

}