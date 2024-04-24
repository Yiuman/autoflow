package io.autoflow.spi.model;

import lombok.Data;

import java.util.Map;

/**
 * 字段校验规则
 *
 * @author yiuman
 * @date 2024/4/24
 */
@Data
public class ValidateRule {
    /**
     * 字段名
     */
    private String field;
    /**
     * 是否必须
     */
    private Boolean required;
    /**
     * 提示文本
     */
    private String message;
    /**
     * 字段类型
     */
    private String fieldType;
    /**
     * 脚本
     */
    private String script;

    /**
     * 校验类型(对应jakarta.validation的注解名)
     */
    private String validateType;

    /**
     * 对应注解的字段
     */
    private Map<String, Object> attributes;
}
