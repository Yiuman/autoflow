package io.autoflow.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yiuman
 * @date 2024/11/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolData {
    private String name;
    private int activeCount;
    private int corePoolSize;
    private int maximumPoolSize;
    private long completedTaskCount;
    private int queueSize;
    private double idleThreadRate;

    public <T extends ThreadPoolExecutor> ThreadPoolData(String name, T threadPoolExecutor) {
        this.name = name;
        this.activeCount = threadPoolExecutor.getActiveCount();
        this.corePoolSize = threadPoolExecutor.getCorePoolSize();
        this.maximumPoolSize = threadPoolExecutor.getMaximumPoolSize();
        this.completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        this.queueSize = threadPoolExecutor.getQueue().size();
    }

    public double getIdleThreadRate() {
        return (double) (maximumPoolSize - activeCount) / maximumPoolSize;
    }
}
