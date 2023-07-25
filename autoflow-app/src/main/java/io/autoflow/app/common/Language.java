package io.autoflow.app.common;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author yiuman
 * @date 2022/6/22
 */
@Component
public class Language {

    private static MessageSource messageSource;

    public Language(MessageSource messageSource) {
        Language.messageSource = messageSource;
    }

    public static String get(String key) {
        if (Objects.isNull(messageSource)) {
            return key;
        }
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
