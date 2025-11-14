package io.autoflow.spi.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.jayway.jsonpath.JsonPath;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;

/**
 * @author yiuman
 * @date 2024/4/16
 */
public final class ExpressUtils {
    private static final Log LOGGER = LogFactory.getLog(ExpressUtils.class);
    private static final ExpressRunner EXPRESS_RUNNER = new ExpressRunner();
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

    static {
        try {
            EXPRESS_RUNNER.addFunctionOfServiceMethod(JSONPATH_EXPRESS_METHOD, ExpressUtils.class.getName(), "extractByJsonPath", new Class[]{String.class}, null);
            EXPRESS_RUNNER.addFunctionOfClassMethod(IS_EMPTY_METHOD, ObjectUtil.class.getName(), "isEmpty", new Class[]{Object.class}, null);
            EXPRESS_RUNNER.addFunctionOfClassMethod(IS_NOT_EMPTY_METHOD, ObjectUtil.class.getName(), "isNotEmpty", new Class[]{Object.class}, null);
        } catch (Exception exception) {
            LOGGER.debug("init expressRunner happen error ", exception);
        }
    }

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

    public static ExpressRunner expressRunner() {
        return EXPRESS_RUNNER;
    }

    public static Object extractByJsonPath(String jsonStr, String strValue) {
        try {
            if (ExpressUtils.isJsonPath(strValue)) {
                return JsonPath.read(jsonStr, strValue);
            }
        } catch (Throwable throwable) {
            LOGGER.debug("read json path happen error ", throwable);
        }
        return null;
    }

    public static Object extractByExpress(IExpressContext<String, Object> iExpressContext, String strValue) {
        try {
            String express = ExpressUtils.getExpressText(strValue);
            if (StrUtil.isNotBlank(express)) {
                return EXPRESS_RUNNER.execute(express, iExpressContext, null, false, false);
            }

        } catch (Throwable throwable) {
            LOGGER.debug(StrUtil.format("execute express happen error, express string '{}' ", strValue), throwable);
        }
        return null;
    }
}
