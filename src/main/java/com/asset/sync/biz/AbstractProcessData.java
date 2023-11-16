package com.asset.sync.biz;

import com.asset.sync.biz.context.BaseProcessContext;
import com.asset.sync.biz.context.MapProcessContext;
import com.idss.common.datafactory.model.SearchCri;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fisher
 * @date 2023-08-11: 14:41
 * abstract sync data to es
 */
public abstract class AbstractProcessData<E extends BaseProcessContext> {

    /**
     * pre handler
     * @return
     */
    protected abstract void preHandler(E e);

    /**
     * process sync core
     */
    protected abstract MapProcessContext process(E data);

    /**
     * process finished handler
     */
    protected abstract void afterHandler();

    /**
     * get cri
     */
    protected SearchCri getSearchCri(E e){
        long currentTime = System.currentTimeMillis();
        SearchCri searchCri = new SearchCri();
        searchCri.setTypes(StringUtils.isBlank(e.getType()) ? BaseProcessContext.DEFAULT_TYPE : e.getType());
        searchCri.setIndexName(e.getIndex());
        searchCri.setIgnoreTimeFilter(false);
        searchCri.setStartTime(currentTime);
        searchCri.setEndTime(currentTime);
        return searchCri;
    }


}
