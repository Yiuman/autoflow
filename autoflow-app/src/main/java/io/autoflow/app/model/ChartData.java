package io.autoflow.app.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static ChartData of(List<Map<String, Object>> data, String dimension) {
        Map<String, Object> first = CollUtil.getFirst(data);
        ChartData chartData = new ChartData();
        Set<String> columns = first.keySet();
        chartData.setIndicator(columns.stream()
                .filter(column -> !StrUtil.equals(column, dimension))
                .toList());
        chartData.setDimension(List.of(dimension));
        chartData.setTotal(data.size());
        chartData.setData(data);
        return chartData;
    }
}
