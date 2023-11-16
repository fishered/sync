package com.asset.sync.biz.config;

import com.idss.common.datafactory.DataSearch;
import com.idss.common.datafactory.DataSearchFactory;
import com.idss.common.datafactory.model.SearchCri;
import com.idss.common.datafactory.utils.PropertiesUtil;
import org.springframework.stereotype.Component;

/**
 * @author fisher
 * @date 2023-08-28: 11:12
 */
@Component
public class ESHelper {
    /***
     * 查询用search
     */
    private static DataSearch dataSearch = DataSearchFactory.buildDataSearch();

    public static DataSearch getDataSearch() {
        return dataSearch;
    }
}
