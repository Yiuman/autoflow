package io.autoflow.spi.utils;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import java.util.regex.Pattern;

/**
 * @author yiuman
 * @date 2024/4/16
 */
public final class ExpressUtils {
    public static final String IS_NOT_EMPTY_METHOD = "IsNotEmpty";
    public static final String IS_EMPTY_METHOD = "IsEmpty";
    public static final String JSONPATH_EXPRESS_METHOD = "JsonPath";
    public static final String JSONPATH_EXPRESS_TPL = "JsonPath(\"{}\")";

    /**
     * 提取jsonpath的表达式
     */
    public static final Pattern JSON_PATH_PATTERN = Pattern.compile("\\$(\\.\\w+|\\[\\d+]|\\['[^']+']|\\[\"[^\"]+\"]|\\.[\\w*]+|\\[\\?\\([^)]+\\)])+");
    /**
     * 提取变量表达式的正则
     */
    public static final Pattern EXPRESS_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private ExpressUtils() {
    }

    public static String isNotEmptyExpress(Object strValue) {
        return StrUtil.format("{}(\"{}\")", IS_NOT_EMPTY_METHOD, strValue);
    }

    public static String isEmptyExpress(Object strValue) {
        return StrUtil.format("{}(\"{}\")", IS_EMPTY_METHOD, strValue);
    }

    public static boolean isJsonPath(String strValue) {
        return ReUtil.isMatch(JSON_PATH_PATTERN, strValue);
    }

    public static boolean isExpress(String strValue) {
        return ReUtil.isMatch(EXPRESS_PATTERN, strValue);
    }

    public static String getExpressText(String strValue) {
        return ReUtil.get(EXPRESS_PATTERN, strValue, 1);
    }

    public static String convertCtxExpressStr(Object obj) {
        if (obj instanceof String strValue) {
            String express = ReUtil.get(EXPRESS_PATTERN, strValue.trim(), 1);
            if (StrUtil.isNotBlank(express)) {
                return express;
            }
            if (isJsonPath(strValue)) {
                return StrUtil.format(JSONPATH_EXPRESS_TPL, strValue.trim());
            }
        }
        String objStr = StrUtil.toString(obj);
        return StrUtil.isBlank(objStr) ? "" : objStr;
    }
}
