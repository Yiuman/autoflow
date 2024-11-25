package io.autoflow.app.rest;

import io.autoflow.app.dao.StatisticsDao;
import io.autoflow.app.model.ChartData;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.ola.common.http.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yiuman
 * @date 2024/11/25
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsDao statisticsDao;
    private final MeterRegistry meterRegistry;

    @GetMapping("/overview")
    public R<ChartData> countOverview() {
        ChartData chartData = new ChartData();
        chartData.setDimension(List.of("metric"));
        chartData.setIndicator(List.of("quantity"));
        chartData.setData(statisticsDao.countOverview());
        return R.ok(chartData);
    }

    @GetMapping("/metrics")
    public R<ChartData> getCpuAndMemoryUsage() {
        ChartData chartData = new ChartData();
        chartData.setData(
                List.of(Map.of(
                        "cpuUsage", getMetricValue("system.cpu.usage"),
                        "memoryMax", getMetricValue("jvm.memory.max", "area", "heap"),
                        "memoryUsed", getMetricValue("jvm.memory.used", "area", "nonheap")
                ))
        );
        return R.ok(chartData);
    }

    private double getMetricValue(String metricName, String... tags) {
        Meter meter = Search.in(meterRegistry).name(metricName).tags(tags).meter();
        if (Objects.isNull(meter)) {
            return 0D;
        }
        return Optional.ofNullable(meter.measure().iterator().next().getValue()).orElse(0D);
    }

}
