package io.autoflow.spi.utils;

import cn.hutool.core.io.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author yiuman
 * @date 2024/10/30
 */
public final class I18nUtils {
    private static final Pattern LOCALE_PATTERN = Pattern.compile("messages_(\\w+_\\w+)\\.properties");
    private static final Map<Class<?>, Map<String, Properties>> I18N_CACHE = new ConcurrentHashMap<>();

    public static Map<String, Properties> getI18n(Class<?> clazz) {
        Map<String, Properties> cacheI18n = I18N_CACHE.get(clazz);
        if (Objects.nonNull(cacheI18n)) {
            return cacheI18n;
        }
        Map<String, Properties> i18n = new HashMap<>();
        URL resource = clazz.getResource("resource/messages");
        if (Objects.isNull(resource)) {
            I18N_CACHE.put(clazz, i18n);
            return i18n;
        }
        Path messagesDir = Paths.get(resource.getPath());
        // 遍历 messages 文件夹下的所有 .properties 文件
        try (Stream<Path> paths = Files.walk(messagesDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".properties"))
                    .forEach(path -> {
                        String filename = FileUtil.getName(path);
                        Matcher matcher = LOCALE_PATTERN.matcher(filename);
                        if (!matcher.matches()) {
                            return;
                        }
                        Properties properties = new Properties();
                        try (InputStream is = Files.newInputStream(path)) {
                            properties.load(is);
                            i18n.put(matcher.group(1), properties);
                        } catch (IOException ignore) {
                        }

                    });
        } catch (IOException ignore) {
        }
        I18N_CACHE.put(clazz, i18n);
        return i18n;
    }
}
