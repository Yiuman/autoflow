package io.autoflow.spi.provider;

import cn.hutool.core.bean.copier.ValueProvider;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jayway.jsonpath.JsonPath;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import io.autoflow.spi.context.Constants;
import io.autoflow.spi.context.ExecutionContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 执行器上下文值提供器
 * 用于将上下文中的变量值/表达式解释后填充到对象
 *
 * @author yiuman
 * @date 2023/7/27
 */
public class ExecutionContextValueProvider implements ValueProvider<String>, IExpressContext<String, Object> {
    private final Map<String, Object> variables = new HashMap<>();
    /**
     * 表达式匹配
     */
    private static final String EXPRESS_REGEX = "^\\$\\{(.*)}";
    /**
     * JSON-PATH匹配
     */
    private static final String JSON_PATH_REGEX = "^\\$\\..*$";
    private static final ExpressRunner EXPRESS_RUNNER = new ExpressRunner();

    public ExecutionContextValueProvider(ExecutionContext executionContext) {
        variables.putAll(executionContext.getParameters());
        variables.put(Constants.INPUT_DATA, executionContext.getInputData());
    }

    @Override
    public Object value(String key, Type valueType) {
        Object result = variables.get(key);

        if (result instanceof String strValue) {
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

        if (Objects.nonNull(result)) {
            result = Convert.convertWithCheck(valueType, result, result, true);
        }

        return result;
    }

    private Object extractByJsonPath(String strValue) {
        try {
            if (ReUtil.isMatch(JSON_PATH_REGEX, strValue)) {
                return JsonPath.read(JSONUtil.parseObj(variables), strValue);
            }
        } catch (Throwable ignore) {
        }
        return null;

    }

    private Object extractByExpress(String strValue) {
        try {
            String express = ReUtil.get(EXPRESS_REGEX, strValue, 1);
            if (StrUtil.isNotBlank(express)) {
                return EXPRESS_RUNNER.execute(express, this, null, false, false);
            }

        } catch (Throwable ignore) {
        }
        return null;
    }


    @Override
    public boolean containsKey(String key) {
        return variables.containsKey(key);
    }

    @Override
    public Object get(Object key) {
        return variables.get(key);
    }

    @Override
    public Object put(String name, Object object) {
        variables.put(name, object);
        return object;
    }
}
