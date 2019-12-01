import com.alibaba.fastjson.JSON;
import com.jj.wifi.es.client.ESUtils;
import com.jj.wifi.props.kafka.Props;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ESUtilsTest {
    @Test
    public void readMapping(){

        String mappingJson = ESUtils.getMappingJson("props/es/qq.json");
        System.out.println(mappingJson);
    }
    @Test
    public void testConvert(){
        Map<String, Object> map = ESUtils.convertData("{\"rksj\":\"1575204855\",\"latitude\":\"24.000000\",\"imsi\":\"000000000000000\",\"accept_message\":\"\",\"phone_mac\":\"aa-aa-aa-aa-aa-aa\",\"device_mac\":\"bb-bb-bb-bb-bb-bb\",\"message_time\":\"1789098762\",\"filename\":\"qq_source1_1111162.txt\",\"absolute_filename\":\"H:\\\\bigdata-dev\\\\ideaworkspace\\\\tanzhou\\\\wifi-root\\\\wifi-resources\\\\target\\\\classes\\\\test.data\\\\qq\\\\qq_source1_1111162.txt\",\"phone\":\"18609765432\",\"device_number\":\"32109231\",\"imei\":\"000000000000000\",\"id\":\"5b226541565541b2a2de18a3ef4bbf39\",\"collect_time\":\"1557305988\",\"send_message\":\"\",\"table\":\"qq\",\"object_username\":\"judy\",\"longitude\":\"23.000000\",\"username\":\"andiy\"}");
        System.out.println(map);
    }
    @Test
    public void createDataJson() throws IOException {
        File file = new File(ESUtils.class.getClassLoader().getResource("test.data/qq/qq_source1_1111162.txt").getPath());
        String type = file.getName().split("_")[0];
        String[] strings = FileUtils.readLines(file).get(0).split("\t");
        String[] fields = Props.getDataTypeProps().getProperty(type).split(",");
        Map<String,String> map = new HashMap<>();
        //扩充内容
        map.put("id", UUID.randomUUID().toString().replace("-", ""));
        map.put("table", type);
        map.put("rksj", (System.currentTimeMillis() / 1000) + "");
        map.put("filename", file.getName());
        map.put("absolute_filename", file.getAbsolutePath());
        if(strings.length == fields.length){
            for(int i=0;i<fields.length;i++){
                map.put(fields[i],strings[i]);
            }
        }
//        System.out.println(map);
        String json = JSON.toJSONString(map);
        System.out.println(json);
    }
}
