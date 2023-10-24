package com.yongkj.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ExecutorBuilder {
    private int core = 10;
    private int max = 10;
    private int queue = Integer.MAX_VALUE;
    private String prefix = "executor-pool-";

    public ExecutorBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ExecutorBuilder core(int core) {
        this.core = core;
        return this;
    }

    public ExecutorBuilder max(int max) {
        this.max = max;
        return this;
    }

    public ExecutorBuilder queue(int queue) {
        this.queue = queue;
        return this;
    }

    public ThreadPoolTaskExecutor build() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setQueueCapacity(queue);
        executor.setMaxPoolSize(max);
        executor.setThreadNamePrefix(prefix);
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }
}
