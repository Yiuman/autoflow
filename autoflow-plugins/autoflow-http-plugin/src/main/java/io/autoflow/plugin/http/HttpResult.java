package io.autoflow.plugin.http;

import io.autoflow.spi.model.FileData;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/2/29
 */
@Data
public class HttpResult {
    private int status;
    private Object body;
    private Map<String, List<String>> headers;
    private FileData fileData;
}
