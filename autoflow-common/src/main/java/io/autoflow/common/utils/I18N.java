package io.autoflow.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author yiuman
 * @date 2023/7/14
 */
@Component
public class I18N {

    private static MessageSource messageSource;

    public I18N(MessageSource messageSource) {
        I18N.messageSource = messageSource;
    }

    public static String get(String key) {
        if (Objects.isNull(messageSource)) {
            return key;
        }
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
