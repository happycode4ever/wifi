package com.jj.wifi.flume.source;

import com.jj.wifi.flume.constant.FlumeConfigureConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FolderSource extends AbstractSource implements Configurable, PollableSource {

    private static final Logger logger = LoggerFactory
            .getLogger(FolderSource.class);

    public FolderSource() {
        logger.info("FolderSource init");
    }

    private int fileSize;
    private String sourceDir;
    private String successDir;
    private String[] fileSuffixes;
    private int eventSize;
    private List<Event> eventList = new ArrayList<>();


    @Override
    public Status process() throws EventDeliveryException {
        try{
            logger.info("process start");
            //获取ftp服务器的数据源目录
            List<File> files = (List<File>) FileUtils.listFiles(new File(sourceDir), fileSuffixes, true);
            logger.info("files size:{}",files.size());
            //设置批处理数量
            if (files.size() > fileSize) {
                files = files.subList(0, fileSize);
            }

            files.forEach(sourceFile -> {
                logger.warn("process file:{}",sourceFile.getName());
                String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                //在成功目录后按日期存放数据
//            File destDir = new File(successDir + File.separator + dateStr);
                try {
                    File destDir = FileUtils.getFile(new File(successDir), dateStr);
                    //读取文件内容
                    List<String> lines = FileUtils.readLines(sourceFile);
                    logger.warn("file lines size:{}",lines.size());
                    String fileName = sourceFile.getName();
                    String absolutePath = destDir + File.separator + fileName;
                    Map<String, String> headers = new HashMap<>();
                    headers.put(FlumeConfigureConstant.SOURCE_EVENT_HEADER_FILENAME, fileName);
                    headers.put(FlumeConfigureConstant.SOURCE_EVENT_HEADER_ABPATH, absolutePath);
                    logger.warn("headers:{}",headers);
                    //每行数据构建事件
                    lines.forEach(line -> {
                        logger.warn("event abpath:{} line:{}",absolutePath,line);
                        SimpleEvent event = new SimpleEvent();
                        event.setBody(line.getBytes());
                        event.setHeaders(headers);
                        eventList.add(event);
                    });
                    //转移文件到成功目录
                    logger.warn("sourceFile:{} destDir:{}",sourceFile.getAbsolutePath(),destDir.getAbsolutePath());
                    FileUtils.moveFileToDirectory(sourceFile, destDir, true);
                } catch (IOException e) {
                    logger.error("IOException!!!",e);
                }
            });
            //如果堆积的事件达到配置值才推送channel
            if (CollectionUtils.isNotEmpty(eventList) && eventList.size() >= eventSize) {
                getChannelProcessor().processEventBatch(eventList);
                eventList.clear();
            }

            //睡眠500ms防止日志过多 后面去掉
            Thread.sleep(500);
            //成功状态
            return Status.READY;
        }catch (Exception e){
            logger.error("Exception!!!",e);
            return Status.BACKOFF;
        }
    }

    //配置读取
    @Override
    public void configure(Context context) {
        //每批次处理文件数
        fileSize = context.getInteger(FlumeConfigureConstant.SOURCE_FILE_SIZE);
        //获取源数据目录
        sourceDir = context.getString(FlumeConfigureConstant.SOURCE_SOURCE_DIR);
        //成功目录
        successDir = context.getString(FlumeConfigureConstant.SOURCE_SUCCESS_DIR);
        //需要读取文件的后缀，不填默认是null，取全部文件
        String fileSuffix = context.getString(FlumeConfigureConstant.SOURCE_FILE_SUFFIX);
        if (StringUtils.isNotEmpty(fileSuffix)) {
            fileSuffixes = fileSuffix.split(",");
        }
        eventSize = context.getInteger(FlumeConfigureConstant.SOURCE_EVENT_SIZE, 2);

    }
}
