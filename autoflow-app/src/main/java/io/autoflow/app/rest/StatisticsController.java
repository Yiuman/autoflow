package io.autoflow.app.rest;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.yomahub.liteflow.thread.ExecutorHelper;
import io.autoflow.app.dao.StatisticsDao;
import io.autoflow.app.model.ChartData;
import io.autoflow.app.model.MetricData;
import io.autoflow.app.model.ThreadPoolData;
import io.autoflow.app.service.ExecutionService;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.ola.common.http.R;
import lombok.RequiredArgsConstructor;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

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
        return R.ok(ChartData.of(statisticsDao.countOverview(), "metric"));
    }

    @GetMapping("/execution")
    public R<ChartData> countExecutionGroupByService() {
        return R.ok(ChartData.of(statisticsDao.countExecutionGroupByService(), "service_id"));
    }

    @GetMapping("/metrics")
    public R<MetricData> getCpuAndMemoryUsage() {
        MetricData metricData = new MetricData();
        metricData.setCpuUsage(getMetricValue("system.cpu.usage"));
        metricData.setMemoryMax(getMetricValue("jvm.memory.max", "area", "heap"));
        metricData.setMemoryUsed(getMetricValue("jvm.memory.used", "area", "nonheap"));
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) TtlExecutors.unwrap(ExecutorHelper.loadInstance().buildWhenExecutor());
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor(ExecutionService.THREAD_POOL_NAME);
        metricData.setWorkflowThreadPool(new ThreadPoolData(ExecutionService.THREAD_POOL_NAME, dtpExecutor));
        metricData.setAsyncTaskThreadPool(new ThreadPoolData("WHEN_TASK_THREAD_POOL", threadPoolExecutor));
        return R.ok(metricData);
    }

    private double getMetricValue(String metricName, String... tags) {
        Meter meter = Search.in(meterRegistry).name(metricName).tags(tags).meter();
        if (Objects.isNull(meter)) {
            return 0D;
        }
        return Optional.ofNullable(meter.measure().iterator().next().getValue()).orElse(0D);
    }

}
