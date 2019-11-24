package com.jj.wifi.kafka;

import com.jj.wifi.props.kafka.Props;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CustomProducer {
    private static final Logger logger = LoggerFactory.getLogger(CustomProducer.class);
    //读取kafka配置 初始化构建唯一的生产者对象
    private static KafkaProducer<String, String> producer;
    static{
        Properties kafkaProps = Props.getKafkaProps();
        producer = new KafkaProducer<String, String>(kafkaProps);
    }
    public static void produce(String topic,String value){
        logger.info("prouder:{} send topic:{} value:{}",producer,topic,value);
        producer.send(new ProducerRecord<String,String>(topic,value));
    }
}
