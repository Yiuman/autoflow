package io.autoflow.liteflow.service.impl;

import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.runtime.Executor;
import io.autoflow.spi.model.ExecutionData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Service
public class LiteFlowExecutor implements Executor {

    @Override
    public Map<String, List<ExecutionData>> execute(Flow flow) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public String getExecutableId(Flow flow) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public List<ExecutionData> executeNode(Node node) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public void startByExecutableId(String executableId) {
        throw new UnsupportedOperationException("Method not implemented.");
    }
}
