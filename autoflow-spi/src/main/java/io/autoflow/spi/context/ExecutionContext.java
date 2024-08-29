package io.autoflow.spi.context;

import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2023/7/11
 */
public interface ExecutionContext {

    /**
     * 执行上下的参数信息（节点入参）
     */
    Map<String, Object> getParameters();

    /**
     * 执行上下的输入信息 （前节点的输出数据）
     * key为节点的ID，value是节点的输出值，之所以是集合是因为节点能自遍历执行多次
     */
    Map<String, List<Object>> getInputData();

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

}