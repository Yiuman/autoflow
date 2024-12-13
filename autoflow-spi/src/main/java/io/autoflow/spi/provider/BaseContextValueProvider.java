package io.autoflow.spi.provider;

import cn.hutool.core.bean.copier.ValueProvider;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.*;
import com.jayway.jsonpath.JsonPath;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import io.autoflow.spi.utils.ExpressUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

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
        //获取上下文中的值
        Object result = get(key);
        //当前为String类型则转换成目标值，有值则直接返回
        Object expressValue = getExpressValue(result, valueType);

        if (Objects.nonNull(expressValue)) {
            return Convert.convertWithCheck(valueType, expressValue, null, true);
        }

        //将上下文值转换成目标类型值
        if (Objects.nonNull(result)) {
            //提取结果为对象
            Class<?> typeClass = TypeUtil.getClass(result.getClass());
            if (!ClassUtil.isSimpleValueType(typeClass)) {
                result = fillBeanValue(result);
            }

            result = Convert.convertWithCheck(valueType, result, null, true);
        }

        return result;
    }

    public Object getExpressValue(Object input, Type type) {
        if (String.class.isAssignableFrom(TypeUtil.getClass(type))) {
            String strInput = (String) input;
            if (ExpressUtils.isExpress(strInput) || ExpressUtils.isJsonPath(strInput)) {
                return getExpressValue(input);
            }

            return getExpressStringValue(String.valueOf(input));
        } else {
            return getExpressValue(input);
        }
    }

    public Object fillBeanValue(Object result) {
        if (Objects.isNull(result)) {
            return result;
        }
        Class<?> typeClass = ClassUtil.getClass(result);
        if (ClassUtil.isSimpleValueType(typeClass)) {
            Object expressValue = getExpressValue(result, typeClass);
            if (Objects.nonNull(expressValue)) {
                return expressValue;
            }
            return result;
        }

        if (ArrayUtil.isArray(result)) {
            Object[] objects = ArrayUtil.newArray(typeClass, ArrayUtil.length(result));
            for (int i = 0; i < ArrayUtil.length(result); i++) {
                Object arrayItem = ArrayUtil.get(result, i);
                ArrayUtil.insert(objects, i, fillBeanValue(arrayItem));
            }
            return objects;

        } else if (Collection.class.isAssignableFrom(typeClass)) {
            Collection<Object> collection = CollUtil.create(typeClass);
            for (int i = 0; i < CollUtil.size(result); i++) {
                Object arrayItem = CollUtil.get((Collection<?>) result, i);
                collection.add(fillBeanValue(arrayItem));
            }
            return collection;
        } else if (Map.class.isAssignableFrom(typeClass)) {
            return convertMapValues((Map<?, ?>) result);
        } else {
            Field[] fieldsDirectly = ReflectUtil.getFieldsDirectly(typeClass, true);
            for (Field field : fieldsDirectly) {
                if (!ClassUtil.isSimpleValueType(field.getType())) {
                    continue;
                }
                Object fieldValue = ReflectUtil.getFieldValue(result, field);
                Object expressValue = getExpressValue(fieldValue, field.getType());
                if (Objects.nonNull(expressValue)) {
                    ReflectUtil.setFieldValue(result, field, Convert.convertWithCheck(TypeUtil.getType(field), expressValue, fieldValue, true));
                }
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> convertMapValues(Map<K, V> map) {
        Map<K, V> newMap = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            V value = entry.getValue();
            newMap.put(entry.getKey(), (V) fillBeanValue(value));
        }
        return newMap;
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

    public String getExpressStringValue(String input) {
        // 先替换 JSONPath 部分
        Matcher jsonPathMatcher = JSON_PATH_PATTERN.matcher(input);
        int jsonPathStart = Integer.MIN_VALUE;
        if (jsonPathMatcher.find()) {
            jsonPathStart = jsonPathMatcher.start();
        }

        Matcher expressMatcher = EXPRESS_PATTERN.matcher(input);
        int expressStart = Integer.MIN_VALUE;
        if (expressMatcher.find()) {
            expressStart = expressMatcher.start();
        }
        String result = input;
        if (expressStart > jsonPathStart) {
            result = replaceExpressValue(result);
            result = replaceJsonPathValue(result);
        } else {
            result = replaceJsonPathValue(result);
            result = replaceExpressValue(result);
        }

        return result;
    }

    private String replaceJsonPathValue(String input) {
        Matcher jsonPathMatcher = JSON_PATH_PATTERN.matcher(input);
        StringBuilder result = new StringBuilder();
        while (jsonPathMatcher.find()) {
            String jsonPathKey = jsonPathMatcher.group();
            Object jsonPathValue = extractByJsonPath(jsonPathKey);
            // 使用 quoteReplacement 处理 jsonPathValue 或 jsonPathKey，防止特殊字符导致错误
            String replacement = jsonPathValue != null
                    ? Matcher.quoteReplacement(jsonPathValue.toString())
                    : Matcher.quoteReplacement("");

            jsonPathMatcher.appendReplacement(result, replacement);
        }
        jsonPathMatcher.appendTail(result);
        return result.toString();
    }

    private String replaceExpressValue(String input) {
        Matcher expressMatcher = EXPRESS_PATTERN.matcher(input);
        // 再替换表达式部分
        StringBuilder result = new StringBuilder();
        while (expressMatcher.find()) {
            String expressKey = expressMatcher.group();
            // 去掉 ${ 和 }
            Object expressValue = extractByExpress(expressKey);
            String replacement = expressValue != null
                    ? Matcher.quoteReplacement(expressValue.toString())
                    : Matcher.quoteReplacement("");

            expressMatcher.appendReplacement(result, replacement);
        }
        expressMatcher.appendTail(result);
        return result.toString().trim();
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
            LOGGER.debug(StrUtil.format("execute express happen error, express string '{}' ", strValue), throwable);
        }
        return null;
    }
}
