package com.asset.sync.biz.format;

import cn.hutool.core.lang.Assert;
import com.asset.sync.biz.context.MapProcessContext;
import com.asset.sync.biz.core.SyncProcess;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author fisher
 * @date 2023-08-11: 16:07
 */
@Component
public class MapFormat implements FormatData<MapProcessContext>, InitializingBean {

    private void isSupportThrowEx(MapProcessContext context){
        context.validateThrowEx();
        Assert.notNull(context, "format context is null!");
        Assert.isFalse(CollectionUtils.isEmpty(context.getData()) && context.getData().size() == 0,
                "format context data is null!");
    }

    @Override
    public MapProcessContext format(MapProcessContext context) {
        isSupportThrowEx(context);
        return context;
    }

    @Override
    public String getDesc() {
        return "MapFormat";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SyncProcess.format.putIfAbsent(getDesc(), this);
    }
}
