package com.asset.sync;

import com.alibaba.fastjson.JSON;
import com.asset.sync.biz.config.ESHelper;
import com.asset.sync.biz.context.MapProcessContext;
import com.asset.sync.biz.core.SyncProcess;
import com.asset.sync.biz.type.BizType;
import com.google.common.collect.Maps;
import com.idss.common.datafactory.DataSearch;
import com.idss.common.datafactory.model.SearchCri;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest()
class AssetSyncApplicationTests {

    @Autowired
    private SyncProcess syncProcess;

    /**
     * 这种方法 测试脚本可能运行完并没有数据全部加进来 因为有异步的补偿线程池并没有执行完成就关闭了线程池
     */
    @Test
    void contextLoads() throws InterruptedException {
        syncProcess.bizSyncProcess(BizType.ASSET, MapProcessContext.builder().index("asset_info4").build());
        //这里的睡眠就是保证失败补偿的thread 可以正常添加数据
        Thread.sleep(60*60*1000);
    }

    @Test
    void test(){
        DataSearch dataSearch = ESHelper.getDataSearch();
        Map<String, Object> resultMap = Maps.newHashMap();
        String unknownAssetId = "1697105770362Sf8z0";
        String expression = " es_id = '" + unknownAssetId + "'";
        List<Map<String, Object>> tempData = dataSearch.query(Arrays.asList("fields"),
                searchCri("common.es.alarm-temp-data-unknown-asset-info.index", expression), "es_id", "asc", 1);
        if (CollectionUtils.isNotEmpty(tempData)) {
            resultMap = JSON.parseObject(String.valueOf(tempData.get(0).get("fields")));
        }
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
        cri.setTypes(index);
        return cri;
    }

}
