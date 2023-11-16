package com.asset.sync.biz;


import com.asset.sync.biz.type.BizType;

import java.util.List;
import java.util.Map;

public interface BizSyncLoad {

    /**
     * 滑动获取列表 可以理解为分页页数或者滑块数
     * @param scroll
     * @return
     */
    List<Map<String, Object>> data(int scroll);

    /**
     * 是否支持直接获取全量(全量下scroll可能为空)
     * @return
     */
    boolean isWhole();

    /**
     * 一次能支持获取多大
     * @return
     */
    Long onceDataCount();

    /**
     * 总量
     * @return
     */
    Long count();

    /**
     * biz type
     * @return
     */
    BizType type();

    /**
     * 是否采用异步推送 默认目前都为false
     */
    boolean async();

    /**
     * 获取上一次同步成功的时间戳
     * @return
     */
    String lastSyncTime();

    void processLastTime();

    /**
     * 是否允许执行
     */
    void isSupportThrowEx();

    /**
     * 开始执行标记
     */
    void startProcess();

    void endProcess();

}
