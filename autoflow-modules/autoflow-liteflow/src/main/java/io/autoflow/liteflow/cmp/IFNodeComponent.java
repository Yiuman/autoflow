package io.autoflow.liteflow.cmp;

import com.yomahub.liteflow.core.NodeIfComponent;
import io.autoflow.liteflow.utils.LiteFlows;
import org.springframework.stereotype.Component;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Component
public class IFNodeComponent extends NodeIfComponent {

    @Override
    public boolean processIf() {
        return LiteFlows.getBooleanValue(this);
    }
}