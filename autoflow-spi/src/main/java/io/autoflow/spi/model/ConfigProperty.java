package io.autoflow.spi.model;

import com.alibaba.fastjson2.JSON;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author yiuman
 * @date 2023/7/11
 */
public class ConfigProperty extends SimpleProperty {
    private static final String NAME_ = "name";
    private static final String TYPE_ = "type";
    private static final String DISPLAY_NAME_ = "displayName";
    private static final String DEFAULT_VALUE_ = "displayName";
    private static final String OPTIONS_ = "displayName";

    public ConfigProperty(Config config) {
        setName(config.getString(NAME_));
        setType(config.getString(TYPE_));
        setDisplayName(config.getString(DISPLAY_NAME_));
        setDescription(config.getString(DISPLAY_NAME_));
        setDefaultValue(config.getAnyRef(DEFAULT_VALUE_));
        setOptions(JSON.parseArray(config.getString(OPTIONS_), Option.class));
    }

    public ConfigProperty(String configStr) {
        this(ConfigFactory.parseString(configStr));
    }
}
