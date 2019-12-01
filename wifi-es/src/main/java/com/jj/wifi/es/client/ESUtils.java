package com.jj.wifi.es.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jj.wifi.props.kafka.Props;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class ESUtils {
    //从配置文件读取参数
    private static Properties esProps = Props.getEsProps();
    private static Properties esFieldMappingProps = Props.getEsFieldMappingProps();
    private static Logger logger = LoggerFactory.getLogger(ESUtils.class);
    private ESUtils() {
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static final class CreateIndexRequest{
        private String index;
        //type默认是index
        private String type = index;
        //mapping可以为null
        private String mappingJson;
        //默认分片5
        private int shards = 5;
        //默认副本1
        private int replicas = 1;
    }

    /**
     * 获取ES的客户端连接
     *
     * @return
     */
    public static TransportClient getTransportClient() {

        String clusterName = esProps.getProperty("cluster.name");
        String[] hosts = esProps.getProperty("host").split(",");
        int port = Integer.parseInt(esProps.getProperty("port"));

        Settings settings = Settings.builder()
                .put("cluster.name", clusterName).build();
        TransportClient client = new PreBuiltTransportClient(settings);

        for (String host : hosts) {
            try {
                client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return null;
            }
        }
        return client;
//                .addTransportAddress(new TransportAddress(InetAddress.getByName("host1"), 9300))
//                .addTransportAddress(new TransportAddress(InetAddress.getByName("host2"), 9300));
    }

    public static String getMappingJson(String relativePath){
        String path = ESUtils.class.getClassLoader().getResource(relativePath).getPath();
        String mappingJson = null;
        try {
            mappingJson = FileUtils.readFileToString(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mappingJson;
    }

    /**
     * 创建索引可选是否一并构建mapping
     */
    public static void createIndexAndMapping(CreateIndexRequest request){
        TransportClient client = getTransportClient();
        String index = request.getIndex();
        String type = request.getType();
        int shards = request.getShards();
        int replicas = request.getReplicas();
        String mappingJson = request.getMappingJson();

        IndicesAdminClient indicesAdminClient = client.admin().indices();
        boolean isExists = indicesAdminClient.prepareExists(index).get().isExists();
        //不存在该索引才创建
        if(!isExists){
            indicesAdminClient.prepareCreate(index)
                    .setSettings(Settings.builder()
                            .put("index.number_of_shards", shards)
                            .put("index.number_of_replicas", replicas)
                    )
                    .get();
            //有mapping再构建mapping
            if(StringUtils.isNotBlank(mappingJson)){
                indicesAdminClient.preparePutMapping(index)
                        .setType(type)
                        .setSource(mappingJson, XContentType.JSON).get();
            }
        }
        client.close();
        logger.info("index:{} type:{} create complete!",index,type);
    }

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
        logger.info("convert result:{}",convertMap);
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
        String fieldType = esFieldMappingProps.getProperty(key);
        Object convertResult = null;
        switch (fieldType.toLowerCase()){
            case "string" : convertResult = value;break;
            case "long": convertResult = Long.parseLong(value);break;
            case "double" : convertResult = Double.parseDouble(value);break;
            case "int" : convertResult = Integer.parseInt(value);break;
        }
        return convertResult;
    }
}
