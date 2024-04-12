package io.autoflow.liteflow.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import io.autoflow.core.model.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Slf4j
class LiteFlowsTest {

    @Test
    public void testToJsonStr() {
        String jsonStr = JSONUtil.toJsonStr(new HashMap() {{
            put("regex", "((https?|ftp|gopher|telnet|file):((//)|(\\\\\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)");
            put("content", "$.inputData.hgIkDW8F[0].raw");
            put("method", "findAll");
        }});
        log.info(jsonStr);

        JSON parse = JSONUtil.parse(jsonStr);
    }

    @Test
    public void testConvertEl() {
        Flow flow = JSONUtil.toBean(ResourceUtil.readUtf8Str("test.json"), Flow.class);
        log.info("\n" + LiteFlows.convertElStr(flow));
    }

    @Test
    public void testConvertSingleNodeEL() {
        Node node = new Node();
        node.setId("test_http_node");
        node.setType(NodeType.SERVICE);
        node.setData(new HashMap<>() {{
            put("url", "https://www.baidu.com");
            put("method", "GET");
        }});
        node.setLabel("http");
        log.info("\n" + LiteFlows.convertElStr(Flow.singleNodeFlow(node)));
    }
}