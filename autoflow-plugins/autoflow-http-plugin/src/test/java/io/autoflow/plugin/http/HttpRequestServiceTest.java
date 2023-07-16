package io.autoflow.plugin.http;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.autoflow.spi.context.OnceExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/13
 */
class HttpRequestServiceTest {

    @Test
    public void testHttpService() {
        HttpRequestService httpRequestService = new HttpRequestService();
        HttpRequestParameter httpRequestParameter = new HttpRequestParameter();
        httpRequestParameter.setUrl("https://www.baidu.com/");
        List<ExecutionData> execute = httpRequestService.execute(OnceExecutionContext.create(ExecutionData.builder()
                .json(JSONObject.parseObject(JSON.toJSONString(httpRequestParameter)))
                .build()));

        Assertions.assertTrue(
                ArrayUtil.isNotEmpty(execute)
                        && StrUtil.isNotBlank(execute.get(0).getRaw())
        );
    }
}