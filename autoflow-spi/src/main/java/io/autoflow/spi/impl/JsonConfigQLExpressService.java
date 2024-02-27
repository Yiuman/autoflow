package io.autoflow.spi.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.InstructionSet;
import io.autoflow.spi.Service;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.context.QLExpressServiceContext;
import io.autoflow.spi.exception.ExecuteException;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.model.Property;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JSON配置的QLExpress表达式插件基类
 *
 * @author yiuman
 * @date 2023/7/25
 */
public abstract class JsonConfigQLExpressService implements Service {

    private static final String NAME = "name";
    private static final String PROPERTIES = "properties";
    private static final String DESCRIPTION = "description";
    private static final String EXPRESS = "express";
    private final JSONObject jsonObject;
    private final ExpressRunner expressRunner;
    private final InstructionSet instructionSet;

    public JsonConfigQLExpressService(Path path) throws Exception {
        this(FileUtil.readUtf8String(path.toFile().getPath()));
    }

    public JsonConfigQLExpressService(String json) throws Exception {
        this(json, new ExpressRunner());
    }

    public JsonConfigQLExpressService(String json, ExpressRunner expressRunner) throws Exception {
        this.jsonObject = JSONUtil.parseObj(json);
        this.expressRunner = expressRunner;
        Assert.notBlank(getName());
        String expressString = jsonObject.getStr(EXPRESS);
        Assert.notBlank(expressString);
        instructionSet = expressRunner.parseInstructionSet(expressString);
    }

    @Override
    public String getName() {
        return jsonObject.getStr(NAME);
    }

    @Override
    public List<Property> getProperties() {
        return jsonObject.getBeanList(PROPERTIES, Property.class);
    }

    @Override
    public String getDescription() {
        return jsonObject.getStr(DESCRIPTION);
    }

    @Override
    public List<ExecutionData> execute(ExecutionContext executionContext) {
        List<String> errorList = new ArrayList<>();
        Object expressResult;
        try {
            expressResult = expressRunner.execute(instructionSet, new QLExpressServiceContext(executionContext), errorList, true, false);
        } catch (Throwable throwable) {
            throw new ExecuteException(
                    String.format("An exception occurred during the execution of the service named '%s'", getName()),
                    throwable,
                    getName()
            );
        }

        ExecutionData executionData = new ExecutionData();
        if (Objects.nonNull(expressResult)) {
            try {
                String jsonString = JSONUtil.toJsonStr(expressResult);
                executionData.setRaw(jsonString);
                executionData.setJson(JSONUtil.parseObj(jsonString));
            } catch (Throwable throwable) {
                executionData.setRaw(expressResult.toString());
            }
        }
        return CollUtil.newArrayList(executionData);

    }
}
