package com.jj.wifi.flume.constant;

import com.jj.wifi.flume.dto.interceptor.MailDTO;
import com.jj.wifi.flume.dto.interceptor.SearchDTO;
import com.jj.wifi.flume.dto.interceptor.WechatDTO;

import java.util.HashMap;
import java.util.Map;

public class DataConstant {
    public static final Map<String,Class> map;
    static {
        map = new HashMap<>();
        map.put("mail", MailDTO.class);
        map.put("wechat", WechatDTO.class);
        map.put("search", SearchDTO.class);
    }
    public static final String ID="id";
    public static final String SOURCE="source";
    public static final String TYPE="TYPE";
    public static final String TABLE="table";
    public static final String FILENAME="filename";
    public static final String RKSJ="rksj";
    public static final String ABSOLUTE_FILENAME="absolute_filename";
}
