package io.autoflow.app.model;

import lombok.Data;

/**
 * @author yiuman
 * @date 2024/11/26
 */
@Data
public class MetricData {
    private double cpuUsage;
    private double memoryMax;
    private double memoryUsed;
    private ThreadPoolData workflowThreadPool;
    private ThreadPoolData asyncTaskThreadPool;
}
