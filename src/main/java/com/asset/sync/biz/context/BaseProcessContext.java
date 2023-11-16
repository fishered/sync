package com.asset.sync.biz.context;

import cn.hutool.core.lang.Assert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.idss.common.datafactory.model.SearchCri;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

/**
 * @author fisher
 * @date 2023-08-11: 14:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseProcessContext {

    public static final String DEFAULT_TYPE = "_doc";

    @NotNull
    private String index;

    private String type = DEFAULT_TYPE;

    @JsonIgnore
    public void validateThrowEx(){
        Assert.notBlank(index, "processed data must have index！");
    }

    /**
     * process rule _time and timestamp
     */
    @JsonIgnore
    public void processTime(){}

    @JsonIgnore
    public String getDesc(){
        return StringUtils.EMPTY;
    }

    @JsonIgnore
    public SearchCri getSearchCri(){
        long currentTime = System.currentTimeMillis();
        SearchCri searchCri = new SearchCri();
        searchCri.setTypes(StringUtils.isBlank(type) ? BaseProcessContext.DEFAULT_TYPE : type);
        searchCri.setIndexName(index);
        searchCri.setIgnoreTimeFilter(false);
        searchCri.setStartTime(currentTime);
        searchCri.setEndTime(currentTime);
        return searchCri;
    }

    public String getType() {
        return DEFAULT_TYPE;
    }
}
