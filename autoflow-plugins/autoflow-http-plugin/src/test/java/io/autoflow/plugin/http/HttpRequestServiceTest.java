package io.autoflow.plugin.http;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.OnceExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author yiuman
 * @date 2023/7/13
 */
@Slf4j
class HttpRequestServiceTest {

    @Test
    public void testRequestHtml() {
        HttpRequestService httpRequestService = new HttpRequestService();
        HttpRequestParameter httpRequestParameter = new HttpRequestParameter();
        httpRequestParameter.setUrl("https://www.baidu.com/");

        ExecutionData execute = httpRequestService.execute(OnceExecutionContext.create(httpRequestParameter));

        Assertions.assertTrue(
                ArrayUtil.isNotEmpty(execute)
                        && StrUtil.isNotBlank(execute.getRaw())
        );
        log.info(JSONUtil.toJsonStr(execute));
    }

    @Test
    public void testRequestFile() {
        HttpRequestService httpRequestService = new HttpRequestService();
        HttpRequestParameter httpRequestParameter = new HttpRequestParameter();
        httpRequestParameter.setUrl("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
        ExecutionData execute = httpRequestService.execute(OnceExecutionContext.create(httpRequestParameter));
        Assertions.assertTrue(
                ArrayUtil.isNotEmpty(execute)
                        && StrUtil.isNotBlank(execute.getRaw())
        );
        log.info(JSONUtil.toJsonStr(execute));
    }
}