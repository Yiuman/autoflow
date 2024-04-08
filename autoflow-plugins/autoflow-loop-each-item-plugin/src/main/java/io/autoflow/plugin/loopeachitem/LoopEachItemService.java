package io.autoflow.plugin.loopeachitem;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.provider.ExecutionContextValueProvider;

/**
 * 循环每一个
 * 此插件为特殊插件，单独执行无意义
 * 流程中执行作为子流程循环流转变量到每一个具体的执行插件中
 * 输出数据为最后一个节点的结果集
 *
 * @author yiuman
 * @date 2024/4/8
 */
public class LoopEachItemService extends BaseService<Void> {

    @Override
    public ExecutionData execute(ExecutionContext executionContext) {
        LoopItem loop = BeanUtil.fillBean(new LoopItem(), new ExecutionContextValueProvider(executionContext), CopyOptions.create());
        return ExecutionData.builder()
                .json(JSONUtil.parse(loop))
                .build();
    }

    @Override
    public ExecutionData execute(Void unused) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public String getName() {
        return "LoopEachItem";
    }
}
