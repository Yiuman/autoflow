package io.autoflow.spi.utils;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author yiuman
 * @date 2024/4/16
 */
public final class ExpressUtils {
    /**
     * 表达式匹配
     */
    public static final String EXPRESS_REGEX = "^\\$\\{(.*)}";
    /**
     * JSON-PATH匹配
     */
    public static final String JSON_PATH_REGEX = "^\\$\\..*$";

    private ExpressUtils() {
    }

    public static boolean isJsonPath(String strValue) {
        return ReUtil.isMatch(JSON_PATH_REGEX, strValue);
    }

    public static boolean isExpress(String strValue) {
        return StrUtil.isNotBlank(ReUtil.get(EXPRESS_REGEX, strValue, 1));
    }

    public static String getExpressText(String strValue) {
        return ReUtil.get(EXPRESS_REGEX, strValue, 1);
    }

    public static String convertCtxExpressStr(Object obj) {
        if (obj instanceof String strValue) {
            String express = ReUtil.get(EXPRESS_REGEX, strValue, 1);
            if (StrUtil.isNotBlank(express)) {
                return express;
            }
            if (isJsonPath(strValue)) {
                return String.format("JsonPathReadCtx(\"%s\")", strValue);
            }
        }

        return StrUtil.toString(obj);
    }
}
