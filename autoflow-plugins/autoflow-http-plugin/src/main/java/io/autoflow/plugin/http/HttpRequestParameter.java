package io.autoflow.plugin.http;

import cn.hutool.http.Header;
import cn.hutool.http.Method;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yiuman
 * @date 2023/7/11
 */
@Data
public class HttpRequestParameter {
    private static final String CHROME_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";
    private Map<String, String> headers = new HashMap<>() {{
        put(Header.USER_AGENT.name(), CHROME_USER_AGENT);
    }};
    private String url;
    private Method method = Method.GET;
    private Map<String, Object> params;
}
