package io.autoflow.plugin.switches;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;

/**
 * @author yiuman
 * @date 2024/3/18
 */
public class SwitchService extends BaseService<SwitchParameter> {
    @Override
    public String getName() {
        return "Switch";
    }

    @Override
    public ExecutionData execute(SwitchParameter switchParameter) {
        SwitchResult switchResult = new SwitchResult(
                switchParameter.getExpress(),
                BooleanUtil.isTrue(BooleanUtil.toBooleanObject(switchParameter.getExpress()))
        );
        return ExecutionData.builder()
                .json(JSONUtil.parse(switchResult))
                .build();
    }
}
