package com.jj.wifi.flume.dto.interceptor;

import lombok.Data;

@Data
public class BaseDTO {
    private String imei;
    private String imsi;
    private String longitude;
    private String latitude;
    private String phone_mac;
    private String device_mac;
    private String device_number;
    private String collect_time;
}
