package com.asset.sync.biz.asset;

import cn.hutool.core.lang.Assert;
import com.asset.sync.biz.BizSyncLoad;
import com.asset.sync.biz.core.SyncProcess;
import com.asset.sync.biz.type.BizType;
import com.asset.sync.biz.util.FieldUtil;
import com.asset.sync.domain.AssetInfoEntity;
import com.asset.sync.service.asset.service.AssetInfoService;
import com.asset.sync.service.asset.service.impl.AssetInfoServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fisher
 * @date 2023-08-14: 10:15
 * asset性能问题是因为asset 大字段和长字符串导致io 同步写入性能其实影响不大
 */
@Component
public class AssetSync implements BizSyncLoad, InitializingBean {

    @Autowired
    private AssetInfoService assetInfoService;
    @Autowired
    private RedisTemplate redisTemplate;

    private Long count = 0L;

    private static final String DESC_TIME = "sync:asset:operaTime";
    private static final String DESC_STATUS = "sync:asset:status";

    /**
     * 处理分为全量和增量 增量主要在redis中处理
     * @param scroll
     * @return
     */
    @Override
    public List<Map<String, Object>> data(int scroll) {
        if (count() == 0){
            return Collections.EMPTY_LIST;
        }
        if (!isWhole() && scroll == 0){
            throw new IllegalArgumentException("没有全量情况下不允许设置scroll为0！");
        }
        if (onceDataCount() == null || onceDataCount() == 0){
            throw new IllegalArgumentException("一次指定的数据大小不能为0！");
        }
        Integer index = Integer.parseInt((scroll - 1) * onceDataCount() + "");
        List<AssetInfoEntity> list = isWhole() ?
                queryOwnerSync(onceDataCount(), index)
                :
                pageAuto(onceDataCount(), index, lastSyncTime());
        if (CollectionUtils.isEmpty(list)){
            return Collections.EMPTY_LIST;
        }
        return list.stream().map(e ->
                FieldUtil.formatTitle(e)).collect(Collectors.toList());
    }

    @Override
    public boolean isWhole() {
        return StringUtils.isEmpty(lastSyncTime());
    }

    @Override
    public Long onceDataCount() {
        return 10000L;
    }

    /**
     * 不必要每次都查询count
     * @return
     */
    @Override
    public Long count() {
        String time = lastSyncTime();
        if (StringUtils.isEmpty(time)){
            if (count == null || count == 0){
                count = assetInfoService.count();
            }
        }
        else {
            count = pageAutoCount(time);
        }
        return count;
    }

    @Override
    public BizType type() {
        return BizType.ASSET;
    }

    @Override
    public boolean async() {
        return false;
    }

    @Override
    public String lastSyncTime() {
        return (String) redisTemplate.opsForValue().get(DESC_TIME);
    }

    @Override
    public void processLastTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = LocalDateTime.now().format(formatter);
        redisTemplate.opsForValue().set(DESC_TIME, formattedNow);
    }

    @Override
    public void isSupportThrowEx() {
        Assert.isNull(redisTemplate.opsForValue().get(DESC_STATUS), "asset同步已经在执行了！");
    }

    @Override
    public void startProcess() {
        redisTemplate.opsForValue().set(DESC_STATUS, 1);
    }

    @Override
    public void endProcess() {
        redisTemplate.delete(DESC_STATUS);
    }

    @Override
    public void afterPropertiesSet() {
        SyncProcess.bizSync.put(BizType.ASSET, this);
    }

    private List<AssetInfoEntity> queryOwnerSync(Long pageSize, Integer startIndex){
        LambdaQueryWrapper<AssetInfoEntity> queryWrapper = new LambdaQueryWrapper<AssetInfoEntity>()
                .orderByDesc(AssetInfoEntity::getId)
                .last("LIMIT " + pageSize + " OFFSET " + startIndex);
        return assetInfoService.list(queryWrapper);
    }

    private List<AssetInfoEntity> pageAuto(Long pageSize, Integer startIndex, String updateTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(updateTime);
            Timestamp timestamp = new Timestamp(date.getTime());

            LambdaQueryWrapper<AssetInfoEntity> queryWrapper = new LambdaQueryWrapper<AssetInfoEntity>()
                    .gt(AssetInfoEntity::getUpdateTime, timestamp)
                    .orderByDesc(AssetInfoEntity::getId)
                    .last("LIMIT " + pageSize + " OFFSET " + startIndex);

            return assetInfoService.list(queryWrapper);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Long pageAutoCount(String updateTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(updateTime);
            Timestamp timestamp = new Timestamp(date.getTime());

            LambdaQueryWrapper<AssetInfoEntity> queryWrapper = new LambdaQueryWrapper<AssetInfoEntity>()
                    .gt(AssetInfoEntity::getUpdateTime, timestamp);
            return assetInfoService.count(queryWrapper);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
