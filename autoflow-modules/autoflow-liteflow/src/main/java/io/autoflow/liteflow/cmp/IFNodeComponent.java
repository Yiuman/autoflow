package io.autoflow.liteflow.cmp;

import cn.hutool.core.util.BooleanUtil;
import com.yomahub.liteflow.core.NodeIfComponent;
import io.autoflow.plugin.switches.SwitchParameter;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import org.springframework.stereotype.Component;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Component("SWITCH_RESULT_IF")
public class IFNodeComponent extends NodeIfComponent {

    @Override
    public boolean processIf() {
        SwitchParameter parameter = getCmpData(SwitchParameter.class);
        FlowExecutionContext contextBean = getContextBean(FlowExecutionContext.class);
        Object value = new ExecutionContextValueProvider(contextBean).get(parameter.getExpress());
        return BooleanUtil.toBooleanObject(value.toString());
    }
}
