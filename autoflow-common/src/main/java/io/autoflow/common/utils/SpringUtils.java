package io.autoflow.common.utils;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Component;

/**
 * @author yiuman
 * @date 2023/7/31
 */
@SuppressWarnings("unchecked")
@Component
public final class SpringUtils extends SpringUtil {
    private SpringUtils() {
    }

    public static <T> T getBean(Class<T> clazz, boolean force) {
        Object bean;
        try {
            bean = getApplicationContext().getBean(clazz);
        } catch (NoSuchBeanDefinitionException var4) {
            bean = force ? getApplicationContext().getAutowireCapableBeanFactory().createBean(clazz) : null;
        }

        return (T) bean;
    }
}
