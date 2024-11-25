package io.autoflow.app.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/11/25
 */
@Data
public class ChartData {
    private List<String> dimension;
    private List<String> indicator;
    private Integer total;
    private List<Map<String, Object>> data;
}
