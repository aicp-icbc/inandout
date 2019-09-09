package com.aicp.icbc.inandout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: 吴开云
 * @Date: 2019/9/9 0009
 * @Version： 1.0
 */
@Configuration
@EnableAsync
@Component
public class SpringAsyncConfig {
    // 核心线程数（默认线程数） 线程池维护线程的最小数量
    private static  int corePoolSize = 9;
    // 最大线程数
    private static  int maxPoolSize = 10;
    // 允许线程空闲时间（单位：默认为秒） 空闲线程的存活时间.
    private static  int keepAliveTime = 100;
    // 缓冲队列数
    private static  int queueCapacity = 200;
    // 线程池中任务的等待时间
    private static  int terminationSeconds = 2;
    // 线程池名前缀
    private static  String threadNamePrefix = "Async-Faq-";


    static {
        //获取线程数
        String tokenFileName = "faqtoken.txt";
        FileReader fr = null;
        try {
            fr = new FileReader(tokenFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader buff = new BufferedReader(fr);
        List<String> list = buff.lines().collect(Collectors.toList());
        maxPoolSize = Integer.valueOf(list.get(3)) + 5;
        corePoolSize = Integer.valueOf(list.get(3)) + 2;

    }

    //初始化线程池
    @Bean(name = "faqTaskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveTime);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(terminationSeconds);
        // 线程池对拒绝任务的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }
}
