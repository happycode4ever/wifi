package com.jj.wifi.flume.dto.interceptor;

import lombok.Data;

@Data
public class SearchDTO extends BaseDTO {
    private String name;
    private String is_marry;
    private String phone;
    private String address;
    private String address_new;
    private String birthday;
    private String car_number;
    private String idcard;
}
