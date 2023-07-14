package io.autoflow.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接（线）
 *
 * @author yiuman
 * @date 2023/7/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Connection {
    private String source;
    private String target;
}
