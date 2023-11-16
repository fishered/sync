package com.asset.sync.biz.format;


import com.asset.sync.biz.context.BaseProcessContext;
import com.asset.sync.biz.context.MapProcessContext;

public interface FormatData<E extends BaseProcessContext> {

    MapProcessContext format(E e);

    String getDesc();

}
