import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Properties;

import com.jj.wifi.kafka.CustomProducer;
import com.jj.wifi.props.kafka.Props;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class MyProducer{
    public static void main(String[] args) {
        Properties props = Props.getKafkaProps();
        HashMap<String,Object> map = new HashMap<>();
        map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,props.getProperty("bootstrap.servers"));
        map.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        map.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        map.put(ProducerConfig.ACKS_CONFIG,props.getProperty("acks.config"));
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
        for(int i=0;i<50;i++)
            CustomProducer.produce("test",LocalDateTime.now()+":"+i);
//        producer.send(new ProducerRecord<String,String>("test", LocalDateTime.now()+":"+i));

    }
}
