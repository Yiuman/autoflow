package io.autoflow.spi.model;

import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.json.JSONUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author yiuman
 * @date 2023/7/11
 */
public class ConfigProperty extends SimpleProperty {

    public ConfigProperty(Config config) {
        setName(config.getString(LambdaUtil.getFieldName(SimpleProperty::getName)));
        setType(config.getString(LambdaUtil.getFieldName(SimpleProperty::getType)));
        setDisplayName(config.getString(LambdaUtil.getFieldName(SimpleProperty::getDisplayName)));
        setDescription(config.getString(LambdaUtil.getFieldName(SimpleProperty::getDescription)));
        setDefaultValue(config.getAnyRef(LambdaUtil.getFieldName(SimpleProperty::getDefaultValue)));
        String componentStr = config.getString(LambdaUtil.getFieldName(SimpleProperty::getComponent));
        setComponent(JSONUtil.toBean(componentStr, Component.class));
    }

    public ConfigProperty(String configStr) {
        this(ConfigFactory.parseString(configStr));
    }

}
