package io.autoflow.core.model;

import lombok.Data;

/**
 * 连接（线）
 *
 * @author yiuman
 * @date 2023/7/13
 */
@Data
public class Connection {
    private String source;
    private String target;
}
