package io.autoflow.spi;

import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.Property;

import java.util.List;

/**
 * 节点定义信息
 *
 * @param <OUTPUT> 输出结果
 * @author yiuman
 * @date 2023/7/11
 */
public interface Service<OUTPUT> {

    /**
     * 节点服务的ID
     *
     * @return id值，默认为全类名
     */
    default String getId() {
        return getClass().getName();
    }

    /**
     * 节点名称
     */
    String getName();

    /**
     * 入参的明细（主要用户前端表单渲染）
     *
     * @return 字段描述信息集合
     */
    List<Property> getProperties();

    /**
     * 出参的明细（主要用于前端变量取值）
     *
     * @return 字段描述信息集合
     */
    List<Property> getOutputProperties();

    /**
     * 传入上下文执行返回结果
     *
     * @param ctx 上下文
     * @return 节点执行的结果
     */
    OUTPUT execute(ExecutionContext ctx);

}