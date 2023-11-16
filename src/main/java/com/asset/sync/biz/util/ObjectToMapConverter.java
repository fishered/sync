package com.asset.sync.biz.util;//package com.idss.asm.demand.biz.sync.util;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * @author fisher
// * @date 2023-08-14: 9:26
// */
//public class ObjectToMapConverter {
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    public static Map<String, Object> convertObjectToMap(Object obj) throws Exception {
//        Map<String, Object> map = objectMapper.convertValue(obj, Map.class);
//        return processMap(map);
//    }
//
//    private static Map<String, Object> processMap(Map<String, Object> map) throws Exception {
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            if (entry.getValue() instanceof JSONArray) {
//                JSONArray jsonArray = (JSONArray) entry.getValue();
//                List<Object> list = jsonArray.toList();
//                for (int i = 0; i < list.size(); i++) {
//                    if (list.get(i) instanceof Map) {
//                        list.set(i, processMap((Map<String, Object>) list.get(i)));
//                    }
//                }
//                map.put(entry.getKey(), list);
//            } else if (entry.getValue() instanceof JSONObject) {
//                JSONObject jsonObject = (JSONObject) entry.getValue();
//                Map<String, Object> subMap = jsonObject.toMap();
//                map.put(entry.getKey(), processMap(subMap));
//            }
//        }
//        return map;
//    }
//}