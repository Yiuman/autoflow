package io.autoflow.spi.provider;

import cn.hutool.core.bean.copier.ValueProvider;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.jayway.jsonpath.JsonPath;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author yiuman
 * @date 2024/4/15
 */
public abstract class BaseContextValueProvider implements ValueProvider<String>, IExpressContext<String, Object> {
    /**
     * 表达式匹配
     */
    private static final String EXPRESS_REGEX = "^\\$\\{(.*)}";
    /**
     * JSON-PATH匹配
     */
    private static final String JSON_PATH_REGEX = "^\\$\\..*$";
    private static final ExpressRunner EXPRESS_RUNNER = new ExpressRunner();

    @Override
    public Object value(String key, Type valueType) {
        Object result = get(key);
        Object expressValue = getExpressValue(result);
        if (Objects.nonNull(expressValue)) {
            return expressValue;
        }

        if (Objects.nonNull(result)) {
            result = Convert.convertWithCheck(valueType, result, result, true);
        }

        return result;
    }

    protected Object getExpressValue(Object key) {
        if (key instanceof String strValue) {
            //JSONPATH
            Object jsonPathValue = extractByJsonPath(strValue);
            if (Objects.nonNull(jsonPathValue)) {
                return jsonPathValue;
            }

            //Express
            Object aviatorValue = extractByExpress(strValue);
            if (Objects.nonNull(aviatorValue)) {
                return aviatorValue;
            }
        }
        return null;
    }

    protected Object extractByJsonPath(String strValue) {
        try {
            if (ReUtil.isMatch(JSON_PATH_REGEX, strValue)) {
                return JsonPath.read(toJsonStr(), strValue);
            }
        } catch (Throwable ignore) {
        }
        return null;
    }

    public abstract String toJsonStr();

    protected Object extractByExpress(String strValue) {
        try {
            String express = ReUtil.get(EXPRESS_REGEX, strValue, 1);
            if (StrUtil.isNotBlank(express)) {
                return EXPRESS_RUNNER.execute(express, this, null, false, false);
            }

        } catch (Throwable ignore) {
        }
        return null;
    }
}
