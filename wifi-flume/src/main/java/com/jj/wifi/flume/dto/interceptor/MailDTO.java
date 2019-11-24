package com.jj.wifi.flume.dto.interceptor;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class MailDTO extends BaseDTO {
    private String send_mail;
    private String send_time;
    private String accept_mail;
    private String accept_time;
    private String mail_content;
    private String mail_type;
}
