package com.asset.sync.biz.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.asset.sync.domain.AssetInfoEntity;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

/**
 * @author fisher
 * @date 2023-08-14: 16:39
 */
public class FieldUtil {

    public static Map<String, Object> formatTitle(AssetInfoEntity assetInfoEntity){
        if (assetInfoEntity == null){
            return Collections.EMPTY_MAP;
        }
        Map<String, Object> map = (Map<String, Object>) JSON.toJSON(assetInfoEntity);

        map.put("_time", System.currentTimeMillis());
        map.put("@timestamp", timestamp());

        String title = String.valueOf(map.get("tileFields"));
        if (StringUtils.isEmpty(String.valueOf(title))){
            return map;
        }

        JSONObject jsonObject = JSONObject.parseObject(title);
        for (String key : jsonObject.keySet()) {
            map.put(key, jsonObject.getString(key));
        }
        map.remove("tileFields");
        return map;
    }

    public static String timestamp() {
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'");
        return now.format(formatter);
    }

    public static void main(String[] args) {
        String timestamp = timestamp();
        System.out.println(timestamp);
    }

}
