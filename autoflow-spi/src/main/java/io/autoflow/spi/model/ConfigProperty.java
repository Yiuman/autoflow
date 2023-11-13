package io.autoflow.spi.model;

import cn.hutool.core.lang.func.LambdaUtil;
import com.alibaba.fastjson2.JSON;
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
        setOptions(JSON.parseArray(config.getString(LambdaUtil.getFieldName(SimpleProperty::getOptions)), Option.class));
    }

    public ConfigProperty(String configStr) {
        this(ConfigFactory.parseString(configStr));
    }

}
