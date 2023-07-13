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

    /**
     * 属性的选项
     *
     * @return 选项集合
     */
    List<Option> getOptions();

    /**
     * 当type是object是此属性必须
     *
     * @return object类型的属性集合
     */
    default List<? extends Property> getProperties() {
        return null;
    }
}