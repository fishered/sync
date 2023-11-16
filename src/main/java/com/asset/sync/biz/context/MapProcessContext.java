package com.asset.sync.biz.context;

import cn.hutool.core.lang.Assert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author fisher
 * @date 2023-08-11: 14:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "childBuilder")
public class MapProcessContext extends BaseProcessContext{

    /**
     * 收集者的返回key
     */
//    private Integer index;

    /**
     * map data
     */
    private List<Map<String, Object>> data;

    @Override
    public String getDesc() {
        return "MapFormat";
    }

    @JsonIgnore
    public List<MapProcessContext> split(int num){
        Assert.isFalse(CollectionUtils.isEmpty(data), "async asset data is null!");
        Assert.isTrue(num != 0 && num != 1, "async asset sublist num is 0 or 1!");

        List<MapProcessContext> contexts = new ArrayList<>();

        int size = data.size();
        for (int i = 0; i < size; i+= num) {
            MapProcessContext mapProcessContext = new MapProcessContext();
            mapProcessContext.setIndex(getIndex());
            mapProcessContext.setType(getType());
            mapProcessContext.setData(data.subList(i, Math.min(i + num, size)));
            contexts.add(mapProcessContext);
        }
        return contexts;
    }

}
