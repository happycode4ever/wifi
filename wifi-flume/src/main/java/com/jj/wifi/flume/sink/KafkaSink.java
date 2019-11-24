package com.jj.wifi.flume.sink;

import com.jj.wifi.kafka.CustomProducer;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class KafkaSink extends AbstractSink implements Configurable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaSink.class);
    private String topic;

    @Override
    public Status process() throws EventDeliveryException {
        Status status = null;
        //获取channel
        Channel ch = getChannel();
        //从channel创建事务
        Transaction txn = ch.getTransaction();
        txn.begin();
        try {
            //获取事件
            Event event = ch.take();
            String value = new String(event.getBody());
            logger.warn("producer send topic:{},value:{},time:{}",topic,value, LocalDateTime.now());
            CustomProducer.produce(topic,value);
            txn.commit();
            status = Status.READY;
        } catch (Throwable t) {
            txn.rollback();
            status = Status.BACKOFF;
            if (t instanceof Error) {
                throw (Error)t;
            }
            //官网sink的bug 事务完成需要关闭
        }finally {
            txn.close();
        }
        return status;
    }

    @Override
    public void configure(Context context) {
        topic = context.getString("kafka.topic");
    }
}
