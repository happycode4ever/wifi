package com.jj.wifi.props.kafka;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Props {
    private Props(){}
    @Getter
    private static Properties kafkaProps;//kafka配置
    @Getter
    private static Properties dataTypeProps;//源数据字段名配置
    @Getter
    private static Properties esProps;//es集群配置
    @Getter
    private static Properties esFieldMappingProps;//es字段类型映射配置
    //静态加载配置
    static{
       kafkaProps = loadProps("props/kafka/kafka.properties");
       dataTypeProps = loadProps("props/data/dataType.properties");
       esProps = loadProps("props/es/es.properties");
       esFieldMappingProps = loadProps("props/es/fieldmapping.properties");
    }
    private static Properties loadProps(String path){
        Properties props = new Properties();
        InputStream is = Props.class.getClassLoader().getResourceAsStream(path);
        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

}
