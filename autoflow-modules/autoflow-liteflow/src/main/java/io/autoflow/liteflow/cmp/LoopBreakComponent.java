package io.autoflow.liteflow.cmp;

import com.yomahub.liteflow.core.NodeBreakComponent;
import io.autoflow.liteflow.utils.LiteFlows;
import org.springframework.stereotype.Component;

/**
 * @author yiuman
 * @date 2024/4/12
 */
@Component
public class LoopBreakComponent extends NodeBreakComponent {

    @Override
    public boolean processBreak() {
        return LiteFlows.getBooleanValue(this);
    }

}
