package io.autoflow.spi.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        URL jarLocation = clazz.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = jarLocation.getPath();
        if (jarPath.endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(jarPath)) {
                // 获取资源文件名
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    // 检查条目是否在 messages 目录下
                    String filename = FileUtil.getName(entry.getName());
                    Matcher matcher = LOCALE_PATTERN.matcher(filename);
                    if (!matcher.matches()) {
                        continue;
                    }
                    Properties properties = new Properties();

                    try (BufferedReader bf = new BufferedReader(
                            new InputStreamReader(jarFile.getInputStream(entry), StandardCharsets.UTF_8)
                    )) {
                        properties.load(bf);
                        i18n.put(matcher.group(1), properties);
                    } catch (IOException ignore) {
                    }
                }
            } catch (Throwable ignore) {
            }
        } else {
            List<File> files = FileUtil.loopFiles(jarPath + "/messages");
            if (CollUtil.isNotEmpty(files)) {
                for (File file : files) {
                    String filename = FileUtil.getName(file.getName());
                    Matcher matcher = LOCALE_PATTERN.matcher(filename);
                    if (!matcher.matches()) {
                        continue;
                    }
                    Properties properties = new Properties();
                    try (BufferedReader bf = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
                    )) {
                        properties.load(bf);
                        i18n.put(matcher.group(1), properties);
                    } catch (IOException ignore) {
                    }
                }
            }
        }

        I18N_CACHE.put(clazz, i18n);
        return i18n;
    }
}
