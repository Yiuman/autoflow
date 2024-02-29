package io.autoflow.spi.model;

import lombok.Builder;
import lombok.Data;

/**
 * 节点异常数据
 *
 * @author yiuman
 * @date 2024/2/29
 */
@Data
@Builder
public class Error {
    private String node;
    private String message;
}
