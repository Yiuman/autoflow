package io.autoflow.spi.model;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiuman
 * @date 2023/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionData {
    private JSONObject json;
    private String raw;
    private File file;
}