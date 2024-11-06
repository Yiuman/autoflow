package io.autoflow.spi.utils;

import cn.hutool.core.io.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log LOGGER = LogFactory.getLog(I18nUtils.class);
    private static final Pattern LOCALE_PATTERN = Pattern.compile("messages_(\\w+_\\w+)\\.properties");
    private static final Map<Class<?>, Map<String, Properties>> I18N_CACHE = new ConcurrentHashMap<>();

    public static Map<String, Properties> getI18n(Class<?> clazz) {
        Map<String, Properties> cacheI18n = I18N_CACHE.get(clazz);
        if (Objects.nonNull(cacheI18n)) {
            return cacheI18n;
        }

        URL jarLocation = clazz.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = jarLocation.getPath();
        Map<String, Properties> i18n = jarPath.endsWith(".jar")
                ? loadPropertiesFromJar(jarPath)
                : loadPropertiesFromDirectory(jarPath);
        I18N_CACHE.put(clazz, i18n);
        return i18n;
    }


    private static Map<String, Properties> loadPropertiesFromJar(String jarPath) {
        Map<String, Properties> i18n = new HashMap<>();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String filename = FileUtil.getName(entry.getName());
                Matcher matcher = LOCALE_PATTERN.matcher(filename);
                if (matcher.matches()) {
                    i18n.put(matcher.group(1), loadPropertiesFromStream(jarFile.getInputStream(entry)));
                }
            }
        } catch (IOException ioException) {
            LOGGER.warn("Error accessing JAR file: " + jarPath, ioException);
        }
        return i18n;
    }

    private static Map<String, Properties> loadPropertiesFromDirectory(String dirPath) {
        Map<String, Properties> i18n = new HashMap<>();
        List<File> files = FileUtil.loopFiles(dirPath + "/messages");
        for (File file : files) {
            String filename = file.getName();
            Matcher matcher = LOCALE_PATTERN.matcher(filename);
            if (matcher.matches()) {
                try {
                    i18n.put(matcher.group(1), loadPropertiesFromStream(new FileInputStream(file)));
                } catch (FileNotFoundException fileNotFoundException) {
                    LOGGER.warn("No message files found in directory: " + dirPath + "/messages", fileNotFoundException);
                }
            }
        }
        return i18n;
    }

    private static Properties loadPropertiesFromStream(InputStream inputStream) {
        Properties properties = new Properties();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            properties.load(reader);
        } catch (IOException ioException) {
            LOGGER.warn("Error loading properties ", ioException);
        }
        return properties;
    }
}
