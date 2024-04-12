package io.autoflow.liteflow.cmp;

import com.yomahub.liteflow.core.NodeWhileComponent;
import io.autoflow.liteflow.utils.LiteFlows;
import org.springframework.stereotype.Component;

/**
 * @author yiuman
 * @date 2024/4/12
 */
@Component
public class WhileBreakComponent extends NodeWhileComponent {

    @Override
    public boolean processWhile() {
        return LiteFlows.getBooleanValue(this);
    }
}
