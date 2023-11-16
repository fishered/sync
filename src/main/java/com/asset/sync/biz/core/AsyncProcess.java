package com.asset.sync.biz.core;//package com.idss.asm.demand.biz.sync.core;
//
//import com.idss.asm.common.util.ESHelper;
//import com.idss.asm.demand.biz.sync.context.MapProcessContext;
//import com.idss.common.datafactory.DataSearch;
//import com.idss.common.datafactory.exception.DataSourceNotFoundException;
//import com.idss.common.datafactory.model.SearchCri;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * @author fisher
// * @date 2023-08-11: 16:22
// * 暂时弃用
// */
//@Component
//@Slf4j
//public class AsyncProcess {
//
//    //拒绝策略后面可以修改成after later
//    public ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
//            10, 20, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000),
//            new ThreadFactory() {
//                private final AtomicInteger mThreadNum = new AtomicInteger(1);
//                @Override
//                public Thread newThread(Runnable r) {
//                    return new Thread(r, "syncAssetThread-" + mThreadNum.getAndIncrement());
//                }
//            },
//            new ThreadPoolExecutor.AbortPolicy());
//
//    public void asyncProcess(List<MapProcessContext> mapProcessContexts) {
//        List<CompletableFuture> futureList = new ArrayList<CompletableFuture>(mapProcessContexts.size() + 1);
//        mapProcessContexts.forEach(e -> {
//            CompletableFuture future = CompletableFuture.runAsync(() -> {
//                //任务
//                singleThreadSync(e);
//            },threadPoolExecutor).exceptionally(ex -> {
//                log.info("async thread is error!" + Thread.currentThread().getName());
//                return null;
//            });
//            futureList.add(future);
//        });
//
//        //阻塞等待结果返回
//        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
//        log.info("async send data es is finish！");
//    }
//
//    private void singleThreadSync(MapProcessContext context){
//        try {
//            SearchCri searchCri = context.getSearchCri();
//            getDataSearch().insert(searchCri, context.getData());
//        } catch (DataSourceNotFoundException e) {
//            throw new RuntimeException("es同步数据失败：" + e);
//        }
//    }
//
//    private DataSearch getDataSearch(){
//        return ESHelper.getDataSearch();
//    }
//
//}