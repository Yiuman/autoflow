package io.autoflow.plugin.function;

import cn.hutool.core.util.ReflectUtil;
import com.ql.util.express.parse.NodeTypeManager;
import io.autoflow.spi.OptionValueProvider;
import io.autoflow.spi.model.Option;
import io.autoflow.spi.utils.ExpressUtils;
import io.autoflow.spi.utils.PropertyUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2025/11/14
 */
public class FunctionOptionProvider implements OptionValueProvider {

    @SuppressWarnings("unchecked")
    @Override
    public List<Option> getOptions() {
        NodeTypeManager nodeTypeManager = ExpressUtils.expressRunner().getNodeTypeManager();
        Map<String, String> functions = (Map<String, String>) ReflectUtil.getFieldValue(nodeTypeManager, "functions");
        return functions.keySet().stream().map(key -> new Option(key, key))
                .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return PropertyUtils.getFieldFullPath(FunctionInput::getFunction);
    }
}
