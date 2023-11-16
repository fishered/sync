package com.asset.sync.biz.compensate;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fisher
 * @date 2023-08-23: 17:21
 * 补偿数据的监听者
 */
@Component
public class CompensateListener implements ApplicationRunner {

    /**
     * 单个线程的监听者
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    /**
     * 阻塞的队列集合
     */
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    /**
     * 执行异常的任务重新调度
     */
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            5, 10, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000),
            new ThreadFactory() {
                private final AtomicInteger mThreadNum = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "syncFailed-" + mThreadNum.getAndIncrement());
                }
            });

    @Override
    public void run(ApplicationArguments args) throws Exception {
        executorService.submit(() -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take();  // 阻塞直到队列中有新的元素
                    try {
                        threadPoolExecutor.submit(task).get();  // 等待任务完成，如果任务抛出异常，get() 方法会重新抛出这个异常
                    } catch (ExecutionException e) {
                        taskQueue.add(task);  // 如果任务执行失败，将任务重新添加到队列
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void addTask(Runnable task) {
        taskQueue.add(task);
    }
}
