package com.jj.wifi.props.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.HashMap;
import java.util.Map;

public class ParseData {
    /**
     * 解析json转换map
     * @param json
     * @return
     */
    public static Map<String, Object> convertData(String json) {
        Map<String, String> dataMap = JSON.parseObject(json, new TypeReference<Map<String, String>>(){});
        String table = dataMap.get("table");
        Map<String, Object> convertMap = new HashMap<>();
        dataMap.forEach((k,v) -> {
            Object convertResult = convertType(table, k, v);
            convertMap.put(k,convertResult);
        });
        return convertMap;
    }
    /**
     * 转换每个字段为配置的数据类型
     * @param table
     * @param field
     * @param value
     * @return
     */
    private static Object convertType(String table,String field,String value){
        String key = table + "."+ field;
        String fieldType = Props.getEsFieldMappingProps().getProperty(key);
        Object convertResult = null;
        switch (fieldType.toLowerCase()){
            case "string" : convertResult = value;break;
//            case "long": convertResult = Long.parseLong(value);break;
            case "long": convertResult = value;break;
            case "double" : convertResult = Double.parseDouble(value);break;
            case "int" : convertResult = Integer.parseInt(value);break;
        }
        return convertResult;
    }
}
