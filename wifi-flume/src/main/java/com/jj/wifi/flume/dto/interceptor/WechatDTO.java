package com.jj.wifi.flume.dto.interceptor;

import lombok.Data;

@Data
public class WechatDTO extends BaseDTO {
    private String username;
    private String phone;
    private String object_username;
    private String send_message;
    private String accept_message;
    private String message_time;
}
