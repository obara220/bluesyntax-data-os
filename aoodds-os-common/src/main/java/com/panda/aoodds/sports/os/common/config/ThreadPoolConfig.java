package com.panda.aoodds.sports.os.common.config;

import com.panda.aoodds.sports.os.common.thread.VisiableThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class ThreadPoolConfig {


    /**
     * MY异步抽水
     */
    @Bean("notifyMyCalculateMarketOddsMessage")
    public TaskExecutor notifyMyCalculateMarketOddsMessage() {
        ThreadPoolTaskExecutor executor = new VisiableThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(128);
        //配置最大线程数
        executor.setMaxPoolSize(256);
        //配置队列大小
        executor.setQueueCapacity(512);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("ThreadPool-notifyMyCalculateMarketOddsMessage-");
        //当任务数量超过MaxPoolSize和QueueCapacity时使用的策略，该策略指不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //线程池初始化
        executor.initialize();
        return executor;
    }

}
