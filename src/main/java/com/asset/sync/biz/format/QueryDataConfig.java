package com.asset.sync.biz.format;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fisher
 * @date 2023-08-14: 15:29
 */
@Configuration
public class QueryDataConfig {

    @Bean(name = "customExecutor")
    public Executor customExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                10, // corePoolSize
                20, // maxPoolSize
                60, // keepAliveTime
                TimeUnit.SECONDS, // keepAliveTime unit
                new LinkedBlockingQueue<>(100), // queue
                new ThreadFactory() {
                    private final AtomicInteger mThreadNum = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "queryAssetThread-" + mThreadNum.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );
        return executor;
    }

}
