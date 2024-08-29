package io.autoflow.plugin.http;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import io.autoflow.spi.context.FlowExecutionContextImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

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
        HttpResult execute = httpRequestService.execute(FlowExecutionContextImpl.create(httpRequestParameter));
        log.info(JSONUtil.toJsonStr(execute));
        Assertions.assertTrue(
                ArrayUtil.isNotEmpty(execute)
                        && Objects.nonNull(execute.getBody())
        );
    }

    @Test
    public void testRequestFile() {
        HttpRequestService httpRequestService = new HttpRequestService();
        HttpRequestParameter httpRequestParameter = new HttpRequestParameter();
        httpRequestParameter.setUrl("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
        HttpResult execute = httpRequestService.execute(FlowExecutionContextImpl.create(httpRequestParameter));
        log.info(JSONUtil.toJsonStr(execute));
        Assertions.assertTrue(
                ArrayUtil.isNotEmpty(execute)
                        && Objects.nonNull(execute.getFileData())
        );
    }
}