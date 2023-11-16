package com.asset.sync.biz.core;

import com.asset.sync.biz.AbstractProcessData;
import com.asset.sync.biz.BizSyncLoad;
import com.asset.sync.biz.compensate.CompensateListener;
import com.asset.sync.biz.compensate.RetryTask;
import com.asset.sync.biz.config.ESHelper;
import com.asset.sync.biz.context.BaseProcessContext;
import com.asset.sync.biz.context.MapProcessContext;
import com.asset.sync.biz.format.FormatData;
import com.asset.sync.biz.type.BizType;
import com.idss.common.datafactory.DataSearch;
import com.idss.common.datafactory.exception.DataSourceNotFoundException;
import com.idss.common.datafactory.model.SearchCri;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fisher
 * @date 2023-08-11: 15:25
 */
@Component
@Slf4j
@DependsOn(value = "ESHelper")
public class SyncProcess<E extends BaseProcessContext> extends AbstractProcessData<E> {

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private final DataSearch dataSearch = ESHelper.getDataSearch();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private CompensateListener compensateListener;

    //拒绝策略后面可以修改成after later
    //这种方式 不能把线程池调的更大了 因为之前遗留的datafactory 池化客户端没有很好完善 大了会导致请求链接失效 切记！！！
    public ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            5, 10, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000),
            new ThreadFactory() {
                private final AtomicInteger mThreadNum = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "syncAssetThread-" + mThreadNum.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.AbortPolicy());
    /**
     * 一个线程最多处理多少条数据
     */
    public static final int SINGLE_PROCESS_NUM = 10000;
    public static Map<BizType, BizSyncLoad> bizSync = new HashMap<>();

    public static Map<String, FormatData> format = new HashMap<>();

    @Override
    protected void preHandler(E e) {
        e.validateThrowEx();
    }

    @Override
    protected MapProcessContext process(E e) {
        e.validateThrowEx();
        String desc = e.getDesc();
        if (StringUtils.isEmpty(desc)){
            //传入了base 强转换
            MapProcessContext processContext = new MapProcessContext();
            processContext.setIndex(e.getIndex());
            return processContext;
        }
        return format.get(desc).format(e);
//        if (context.getData().size() <= SINGLE_PROCESS_NUM)
//            syncData(context);
//            //全部采用异步的方式
////            asyncProcess.asyncProcess(Arrays.asList(context));
//        else
//            asyncProcess.asyncProcess(context.split(SINGLE_PROCESS_NUM));
    }

    @Override
    protected void afterHandler() {
    }


    /**
     * get search
     * @return
     */
    private DataSearch getDataSearch(){
        return ESHelper.getDataSearch();
    }

    /**
     * 同步的处理方法
     * @param context
     * @throws DataSourceNotFoundException
     */
    public void syncData(MapProcessContext context) throws DataSourceNotFoundException {
        SearchCri searchCri = super.getSearchCri((E) context);
        getDataSearch().insert(searchCri, context.getData());
    }

    /**
     * auto sync data 2 es
     * @param type
     * @param e
     */
    public void bizSyncProcess(BizType type, E e){
        if (type == null || bizSync.get(type) == null || StringUtils.isEmpty(e.getIndex())){
            throw new IllegalArgumentException("不支持的类型处理！");
        }
        MapProcessContext process = process(e);
        BizSyncLoad bizSyncLoad = bizSync.get(type);

        Long scroll = (bizSyncLoad.count() / bizSyncLoad.onceDataCount()) + 1;
        if (bizSyncLoad.count() == 0 || scroll < 1){
            throw new IllegalArgumentException("数量解析错误！数据不存在！");
        }

        bizSyncLoad.isSupportThrowEx();
        try {
            bizSyncLoad.startProcess();

            List<CompletableFuture> futureList = new ArrayList<CompletableFuture>((int) (scroll + 1));

            for (int i = 1; i <= scroll; i++) {
                futureList.add(asyncFuture(bizSyncLoad, i, process));
            }
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();

            bizSyncLoad.processLastTime();
            log.info("[syncData] send data es is finish！");
        } finally {
            bizSyncLoad.endProcess();
        }
    }

    /**
     * manual sync
     * @param e
     */
    public void manualThrowEx(E e){
        e.validateThrowEx();
        MapProcessContext process = process(e);
        if (CollectionUtils.isEmpty(process.getData())){
            throw new IllegalArgumentException("没有找到要同步的数据！");
        }
        singleThreadSync(process);
    }

    private CompletableFuture asyncFuture(BizSyncLoad bizSyncLoad, int scroll, MapProcessContext context){
        CompletableFuture future = CompletableFuture.runAsync(() -> {
            log.info("[syncData]" + Thread.currentThread().getName() + "start sync work！");
            //任务
            List<Map<String, Object>> data = bizSyncLoad.data(scroll);
            if (CollectionUtils.isEmpty(data)){
                return;
            }
            log.info("[syncData]" + Thread.currentThread().getName() + "sync thread get data num:" + data.size());
            context.setData(data);
            try {
                if (bizSyncLoad.async())
                    singleThreadAsync(context);
                else
                    singleThreadSync(context);

            } catch (Exception e) {
                /**
                 * 出现异常 过段时间后再处理 后面考虑将错误信息缓存起来 专门有一个线程去刷失败的任务
                 */
                log.info("[syncData] otherException error: " + e);
                log.info("[syncData] "+Thread.currentThread().getName() + "ready process fail context");
                ProcessFailContext failContext = ProcessFailContext.builder()
                        .bizType(bizSyncLoad.type())
                        .lastTime(bizSyncLoad.lastSyncTime())
                        .scroll(scroll)
                        .count(bizSyncLoad.onceDataCount())
                        .index(context.getIndex())
                        .context(context)
                        .build();
                RetryTask retryTask = new RetryTask(failContext, bizSyncLoad);
                compensateListener.addTask(retryTask);
//                scheduler.schedule(() -> singleThreadAsync(context), 10, TimeUnit.SECONDS);
            }
        },threadPoolExecutor).exceptionally(ex -> {
            log.info("[syncData] async thread is error!" + Thread.currentThread().getName() + ex);
            return null;
        });
        return future;
    }

    private void singleThreadSync(MapProcessContext context){
        try {
            SearchCri searchCri = context.getSearchCri();
//            getDataSearch().insert(searchCri, context.getData());
            getDataSearch().insertReserveIndexName(searchCri, context.getData());
        } catch (DataSourceNotFoundException e) {
            throw new RuntimeException("es同步数据失败：" + e);
        }
    }

    private void singleThreadAsync(MapProcessContext context){
        DataSearch dataSearch = getDataSearch();
        String index = context.getIndex();
        if (!dataSearch.indexIsExist(index)){
            dataSearch.createIndex(index);
        }
        dataSearch.insertAsyncBatch(context.getData(), index);
    }

    public void insertAsyncBatch(List<Map<String, Object>> data) {
        if (CollectionUtils.isEmpty(data)){
            return;
        }
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                log.info("insertAsyncBatch beforeBulk");
                // Called before the bulk is executed
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                log.info("insertAsyncBatch afterBulk success");
                // Called after the bulk is executed successfully
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                // Called when the bulk failed
                log.info("insertAsyncBatch afterBulk fail");
            }
        };
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                        (request, bulkListener) ->
                                dataSearch.getClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                        listener)
                .setBulkActions(10000) // Execute the bulk every 10,000 requests
                .setBulkSize(new ByteSizeValue(100, ByteSizeUnit.MB)) // Execute the bulk every 5MB
                .build();

        for (Map<String, Object> document : data) {
            IndexRequest indexRequest = new IndexRequest("fisher")
                    .source(document);
            bulkProcessor.add(indexRequest);
        }
        bulkProcessor.flush();
        try {
            bulkProcessor.awaitClose(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
