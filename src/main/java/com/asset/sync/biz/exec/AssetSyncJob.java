package com.asset.sync.biz.exec;

import com.asset.sync.biz.context.MapProcessContext;
import com.asset.sync.biz.core.SyncProcess;
import com.asset.sync.biz.type.BizType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author fisher
 * @date 2023-08-23: 18:24
 */
@Service
@Slf4j
public class AssetSyncJob {

    /**
     * corn 0 0 0 * * ?
     */

    private static final String INDEX = "asset_info";

    @Autowired
    private SyncProcess syncProcess;

    /**
     * 定时同步asset的任务
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void execute() {
        log.info("[syncData] sync exec :" + LocalDateTime.now());
//        throw new RuntimeException("测试定时任务，手动异常！");
        syncProcess.bizSyncProcess(BizType.ASSET,
                MapProcessContext.builder()
                .index(INDEX)
                .build()
        );
    }

}
