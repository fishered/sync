package com.asset.sync.controller;

import com.alibaba.fastjson.JSON;
import com.asset.sync.biz.config.ESHelper;
import com.asset.sync.biz.context.MapProcessContext;
import com.asset.sync.biz.core.SyncProcess;
import com.asset.sync.biz.type.BizType;
import com.asset.sync.param.req.ManualSyncAutoReq;
import com.asset.sync.param.req.ManualSyncReq;
import com.google.common.collect.Maps;
import com.idss.common.datafactory.DataSearch;
import com.idss.common.datafactory.model.SearchCri;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author fisher
 * @date 2023-08-22: 16:01
 */
@RestController
@RequestMapping("sync")
public class SyncController {

    @Autowired
    private SyncProcess syncProcess;

    /**
     * 手动触发自动获取数据同步接口
     * @param req
     */
    @PostMapping("/manual_auto")
    public void manual(@Validated @RequestBody ManualSyncAutoReq req){
        syncProcess.bizSyncProcess(req.getBizType(), req.getP());
    }

    /**
     * 手动触发指定数据同步接口
     * @param req
     */
    @PostMapping("/manual_data")
    public void manualData(@Validated @RequestBody ManualSyncReq req){
        syncProcess.manualThrowEx(req.getP());
    }

    /**
     * 手动触发asset 同步
     */
    @GetMapping("/manual_asset")
    public void asset(){
        syncProcess.bizSyncProcess(BizType.ASSET,
                MapProcessContext.builder()
                        .index("asset_info")
                        .build());
    }

    @GetMapping("test")
    public void test(){
        test2();
    }

    void test2(){
        DataSearch dataSearch = ESHelper.getDataSearch();
        Map<String, Object> resultMap = Maps.newHashMap();
        String unknownAssetId = "1697105770362Sf8z0";
        String expression = " es_id.keyword = '" + unknownAssetId + "'";
        List<Map<String, Object>> tempData = dataSearch.query(Collections.singletonList("fields"),
                searchCri("alarm-temp-data-unknown-asset-info", expression), "es_id", "asc", 1);
        if (CollectionUtils.isNotEmpty(tempData)) {
            resultMap = JSON.parseObject(String.valueOf(tempData.get(0).get("fields")));
        }
        System.out.println(resultMap);
    }
    protected SearchCri searchCri(String esIndex, String... expression) {
        return searchCri2(esIndex, expression);
    }
    public static SearchCri searchCri2(String esIndex, String... expression) {
        SearchCri cri = getSearchCri(esIndex);
        cri.setDateList(Collections.singletonList(""));
        cri.setIgnoreTimeFilter(true);
        if (expression.length == 1 && expression[0] != null) {
            cri.setExpression(expression[0]);
        }
        return cri;
    }

    public static SearchCri getSearchCri(String index) {
        SearchCri cri = new SearchCri();
        cri.setIndexName(index);
        cri.setTypes("common.es.alarm-temp-data-unknown-asset-info.type");
        return cri;
    }

}
