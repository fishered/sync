package com.asset.sync.biz.format;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.asset.sync.biz.context.DocumentProcessContext;
import com.asset.sync.biz.context.MapProcessContext;
import com.asset.sync.biz.core.SyncProcess;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fisher
 * @date 2023-08-11: 15:57
 */
@Component
public class DocumentFormat implements FormatData<DocumentProcessContext>, InitializingBean {

    private void isSupportThrowEx(DocumentProcessContext context){
        context.validateThrowEx();
        Assert.notNull(context, "format context is null!");
        Assert.isFalse(CollectionUtils.isEmpty(context.getData()) && context.getData().size() == 0,
                "format context data is null!");
    }

    @Override
    public MapProcessContext format(DocumentProcessContext context) {
        isSupportThrowEx(context);
        List<?> data = context.getData();

        MapProcessContext mapProcessContext = new MapProcessContext();
        mapProcessContext.setType(context.getType());
        mapProcessContext.setIndex(context.getIndex());
        mapProcessContext.setData(
                data.stream().map(e -> (Map<String, Object>) JSON.toJSON(e)).collect(Collectors.toList())
        );
        return mapProcessContext;
    }

    @Override
    public String getDesc() {
        return "DocumentFormat";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SyncProcess.format.putIfAbsent(getDesc(), this);
    }
}
