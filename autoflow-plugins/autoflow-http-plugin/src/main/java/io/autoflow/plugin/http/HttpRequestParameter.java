package io.autoflow.plugin.http;

import cn.hutool.http.Header;
import cn.hutool.http.Method;
import io.autoflow.spi.annotation.Textarea;
import io.autoflow.spi.model.NamedValue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/11
 */
@Data
public class HttpRequestParameter {
    private static final String CHROME_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

    private List<NamedValue<String>> headers = new ArrayList<>() {{
        add(new NamedValue<>(Header.USER_AGENT.name(), CHROME_USER_AGENT));
    }};
    @NotBlank
    private String url;
    private Method method = Method.GET;
    private List<NamedValue<Object>> params;
    @Textarea
    private String body;
}
