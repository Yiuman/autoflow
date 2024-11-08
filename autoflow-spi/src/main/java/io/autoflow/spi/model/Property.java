package io.autoflow.spi.model;

import java.util.List;

/**
 * 节点的属性
 *
 * @author yiuman
 * @date 2023/7/11
 */
public interface Property {
    /**
     * 属性类型
     *
     * @return 类型
     */
    String getType();

    /**
     * 属性的ID（默认为类型的属性全路径）
     *
     * @return ID值
     */
    String getId();

    /**
     * 属性名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 属性展示的属性名
     *
     * @return 展示的属性名
     */
    String getDisplayName();

    /**
     * 属性描述
     *
     * @return 属性的描述
     */
    String getDescription();

    /**
     * 默认值
     *
     * @return 定义的默认值
     */
    Object getDefaultValue();

    default List<ValidateRule> getValidateRules() {
        return null;
    }

    /**
     * 当type是object是此属性必须
     *
     * @return object类型的属性集合
     */
    default List<? extends Property> getProperties() {
        return null;
    }

    /**
     * 返回null，默认根据属性类型自动适配
     *
     * @return 前端的组件数据
     */
    default Component getComponent() {
        return null;
    }

}