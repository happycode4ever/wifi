package com.jj.wifi.flume.constant;

public class FlumeConfigureConstant {
    /**
     * source部分配置
     */
    public static final String SOURCE_FILE_SIZE = "fileSize";
    public static final String SOURCE_SOURCE_DIR = "sourceDir";
    public static final String SOURCE_SUCCESS_DIR = "successDir";
    public static final String SOURCE_FAIL_DIR = "failDir";
    public static final String SOURCE_FILE_SUFFIX = "fileSuffix";
    public static final String SOURCE_EVENT_SIZE = "eventSize";
    public static final String SOURCE_EVENT_HEADER_FILENAME = "fileName";
    public static final String SOURCE_EVENT_HEADER_ABPATH = "absolutePath";
    /**
     * interceptor部分配置
     */
    public static final String INTERCEPTOR_DATA_VALUE_SPLITTER = "\t";
    public static final String INTERCEPTOR_DATA_FIELD_SPLITTER = ",";
    /**
     * sink部分配置
     */
    public static final String SINK_KAFKA_TOPIC = "kafka.topic";

}
