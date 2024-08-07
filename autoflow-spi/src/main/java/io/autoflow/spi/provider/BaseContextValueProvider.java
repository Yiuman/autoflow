package io.autoflow.spi.provider;

import cn.hutool.core.bean.copier.ValueProvider;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.jayway.jsonpath.JsonPath;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import io.autoflow.spi.utils.ExpressUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Type;
import java.util.Objects;

import static io.autoflow.spi.utils.ExpressUtils.*;

/**
 * @author yiuman
 * @date 2024/4/15
 */
public abstract class BaseContextValueProvider implements ValueProvider<String>, IExpressContext<String, Object> {
    private static final Log LOGGER = LogFactory.getLog(BaseContextValueProvider.class);
    private final ExpressRunner expressRunner = new ExpressRunner();

    public BaseContextValueProvider() {
        try {
            expressRunner.addFunctionOfServiceMethod(JSONPATH_EXPRESS_METHOD, this, "extractByJsonPath", new Class[]{String.class}, null);
            expressRunner.addFunctionOfClassMethod(IS_EMPTY_METHOD, ObjectUtil.class.getName(), "isEmpty", new Class[]{Object.class}, null);
            expressRunner.addFunctionOfClassMethod(IS_NOT_EMPTY_METHOD, ObjectUtil.class.getName(), "isNotEmpty", new Class[]{Object.class}, null);
        } catch (Exception exception) {
            LOGGER.debug("init expressRunner happen error ", exception);
        }
    }

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

    public Object getExpressValue(Object key) {
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

    public Object extractByJsonPath(String strValue) {
        try {
            if (ExpressUtils.isJsonPath(strValue)) {
                return JsonPath.read(toJsonStr(), strValue);
            }
        } catch (Throwable throwable) {
            LOGGER.debug("read json path happen error ", throwable);
        }
        return null;
    }

    public abstract String toJsonStr();

    protected Object extractByExpress(String strValue) {
        try {
            String express = ExpressUtils.getExpressText(strValue);
            if (StrUtil.isNotBlank(express)) {
                return expressRunner.execute(express, this, null, false, false);
            }

        } catch (Throwable throwable) {
            LOGGER.debug("execute express happen error ", throwable);
        }
        return null;
    }
}
