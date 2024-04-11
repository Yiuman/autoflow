package io.autoflow.liteflow.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.core.model.Flow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author yiuman
 * @date 2024/4/11
 */
@Slf4j
class LiteFlowConvertersTest {

    @Test
    public void testConvertEl() {
        Flow flow = JSONUtil.toBean(ResourceUtil.readUtf8Str("test.json"), Flow.class);
        log.info("\n" + LiteFlowConverters.convertEl(flow));

    }
}