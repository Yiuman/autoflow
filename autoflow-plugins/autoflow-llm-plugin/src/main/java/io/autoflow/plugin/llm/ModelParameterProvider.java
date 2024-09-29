package io.autoflow.plugin.llm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.LinkageProvider;
import io.autoflow.spi.OptionValueProvider;
import io.autoflow.spi.model.Option;
import io.autoflow.spi.model.Property;
import io.autoflow.spi.utils.PropertyUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模型参数提供器
 *
 * @author yiuman
 * @date 2024/9/27
 */
public class ModelParameterProvider implements OptionValueProvider, LinkageProvider<String> {

    private static final List<ModelConfig> MODEL_CONFIGS = new ArrayList<>();
    private static final List<Option> OPTIONS = new ArrayList<>();
    private static final Map<String, List<Property>> MODEL_PROPERTIES_MAP = new HashMap<>();

    static {
        List<URL> modelConfigs = ResourceUtil.getResources("model");
        for (URL modelConfig : modelConfigs) {
            List<File> files = FileUtil.loopFiles(modelConfig.getPath());
            for (File file : files) {
                String jsonStr = FileUtil.readString(file, StandardCharsets.UTF_8);
                MODEL_CONFIGS.add(JSONUtil.toBean(jsonStr, ModelConfig.class));
            }
        }
        OPTIONS.addAll(
                MODEL_CONFIGS.stream().map(modelConfig -> {
                    Option option = new Option();
                    option.setName(modelConfig.getModelName());
                    option.setValue(modelConfig.getModelName());
                    return option;
                }).toList()
        );

        MODEL_PROPERTIES_MAP.putAll(
                MODEL_CONFIGS.stream()
                        .collect(Collectors.toMap(Model::getModelName, ModelConfig::getProperties))
        );
    }

    public static ModelConfig getModelConfigByModelName(String modelName) {
        return CollUtil.findOne(MODEL_CONFIGS, modelConfig -> Objects.equals(modelName, modelConfig.getModelName()));
    }

    @Override
    public List<Property> getLinkageProperties(String model) {
        if (Objects.isNull(model)) {
            return null;
        }
        return MODEL_PROPERTIES_MAP.get(model);
    }

    @Override
    public List<Option> getOptions() {
        return OPTIONS;
    }

    @Override
    public String getId() {
        return PropertyUtils.getFieldFullPath(LlmParameter::getModel);
    }
}
