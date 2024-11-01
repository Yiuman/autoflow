package io.autoflow.spi;

import io.autoflow.spi.utils.I18nUtils;

import java.util.Map;
import java.util.Properties;

/**
 * @author yiuman
 * @date 2024/10/30
 */
public interface I18n {

    default Map<String, Properties> getI18n() {
        return I18nUtils.getI18n(this.getClass());
    }

}