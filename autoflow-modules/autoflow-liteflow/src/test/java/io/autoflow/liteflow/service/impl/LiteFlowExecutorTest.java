package io.autoflow.liteflow.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.yomahub.liteflow.core.FlowExecutorHolder;
import com.yomahub.liteflow.property.LiteflowConfig;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.model.NodeType;
import io.autoflow.spi.model.ExecutionData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/4/12
 */
@Slf4j
class LiteFlowExecutorTest {
    private final LiteFlowExecutor liteFlowExecutor = new LiteFlowExecutor(FlowExecutorHolder.loadInstance(new LiteflowConfig()));

    @Test
    public void testRunFlow() {
        String flowJson = ResourceUtil.readUtf8Str("test.json");
        Map<String, List<ExecutionData>> execute = liteFlowExecutor.execute(JSONUtil.toBean(flowJson, Flow.class));
        log.info(JSONUtil.toJsonStr(execute));
    }

    @Test
    public void testExecuteSingleNode() {
        Node node = new Node();
        node.setId("test_http_node");
        node.setServiceId("io.autoflow.plugin.http.HttpRequestService");
        node.setType(NodeType.SERVICE);
        node.setData(new HashMap<>() {{
            put("url", "https://www.baidu.com");
            put("method", "GET");
        }});
        node.setLabel("http");
        List<ExecutionData> executionData = liteFlowExecutor.executeNode(node);
        log.info(JSONUtil.toJsonStr(executionData));
    }

}