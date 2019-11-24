package com.jj.wifi.flume.interceptor;

import com.alibaba.fastjson.JSON;
import com.jj.wifi.flume.constant.DataConstant;
import com.jj.wifi.flume.constant.FlumeConfigureConstant;
import com.jj.wifi.props.kafka.Props;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.*;

public class ETLInterceptor implements Interceptor {



    @Override
    public void initialize() {

    }

    @Override
    public Event intercept(Event event) {
        if (event != null) {
            String line = new String(event.getBody());
            String[] values = line.split("\t");
            Map<String, String> headers = event.getHeaders();
            String fileName = headers.get(FlumeConfigureConstant.SOURCE_EVENT_HEADER_FILENAME);
            String absoultePath = headers.get(FlumeConfigureConstant.SOURCE_EVENT_HEADER_ABPATH);
            String type = fileName.split("_")[0];
            String[] fields = Props.getDataTypeProps().getProperty(type).split(",");
            Map<String, String> contentMap = new HashMap<>();
            //粗略筛选字段个数和数据个数对应的数据
            if (fields.length == values.length) {
                for (int i = 0; i < fields.length; i++) {
                    contentMap.put(fields[i], values[i]);
                }
                //扩充内容
                contentMap.put(DataConstant.ID, UUID.randomUUID().toString().replace("-", ""));
                contentMap.put(DataConstant.TABLE, type);
                contentMap.put(DataConstant.RKSJ, (System.currentTimeMillis() / 1000) + "");
                contentMap.put(DataConstant.FILENAME, fileName);
                contentMap.put(DataConstant.ABSOLUTE_FILENAME, absoultePath);
            } else {
                //TODO 错误数据处理
            }
            String json = JSON.toJSONString(contentMap);
            event.setBody(json.getBytes());
        }
        return event;
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        List<Event> eventList = new ArrayList<>();
        for (Event event : events) {
            eventList.add(intercept(event));
        }
        return eventList;
    }

    @Override
    public void close() {

    }

    //内部类构建器
    public static class Builder implements Interceptor.Builder {


        @Override
        public Interceptor build() {
            return new ETLInterceptor();
        }

        //读取配置
        @Override
        public void configure(Context context) {

        }
    }
}
