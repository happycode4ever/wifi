package com.jj.wifi.props.kafka;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class SortedProps {
    private SortedProps() {
    }

    private static CompositeConfiguration conf;

    static {
        try {
            //使用common-configuration构建合并配置
            conf = new CompositeConfiguration();
            //添加对应的conf
            PropertiesConfiguration fieldmappingConf = new PropertiesConfiguration("props/es/fieldmapping.properties");
            conf.addConfiguration(fieldmappingConf);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static CompositeConfiguration getCompositeConf(){
        return conf;
    }
}
