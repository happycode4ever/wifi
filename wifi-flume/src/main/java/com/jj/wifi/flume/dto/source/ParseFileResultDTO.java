package com.jj.wifi.flume.dto.source;

import lombok.Data;

import java.util.List;

@Data
public class ParseFileResultDTO {
    private String path;
    private List<String> content;
}
