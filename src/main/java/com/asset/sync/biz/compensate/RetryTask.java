package com.asset.sync.biz.compensate;

import com.asset.sync.biz.BizSyncLoad;
import com.asset.sync.biz.config.ESHelper;
import com.asset.sync.biz.context.MapProcessContext;
import com.asset.sync.biz.core.ProcessFailContext;
import com.idss.common.datafactory.exception.DataSourceNotFoundException;
import com.idss.common.datafactory.model.SearchCri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fisher
 * @date 2023-08-23: 17:30
 */
public class RetryTask implements Runnable{
    private static Logger logger = LoggerFactory.getLogger(RetryTask.class);
    private final ProcessFailContext failContext;
    private final BizSyncLoad bizSyncLoad;

    public RetryTask(ProcessFailContext failContext, BizSyncLoad bizSyncLoad) {
        this.failContext = failContext;
        this.bizSyncLoad = bizSyncLoad;
    }

    @Override
    public void run() {
        logger.info("[sync] fail thread retry data: " + failContext.toString());
        MapProcessContext context = failContext.getContext();
        if (context != null){
            retrySync(context);
        }
    }

    private void retrySync(MapProcessContext context){
        try {
            SearchCri searchCri = context.getSearchCri();
            ESHelper.getDataSearch().insertReserveIndexName(searchCri, context.getData());
        } catch (DataSourceNotFoundException e) {
            throw new RuntimeException("es同步数据失败：" + e);
        }
    }
}
