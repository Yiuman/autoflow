package io.autoflow.liteflow.cmp;

import com.yomahub.liteflow.core.NodeBooleanComponent;
import io.autoflow.liteflow.utils.LiteFlows;
import org.springframework.stereotype.Component;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Component
public class IFNodeComponent extends NodeBooleanComponent {

    @Override
    public boolean processBoolean() {
        return LiteFlows.getBooleanValue(this);
    }
}
