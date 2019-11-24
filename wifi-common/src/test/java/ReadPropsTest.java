import com.jj.wifi.props.kafka.Props;

import java.util.Properties;

public class ReadPropsTest {
    public static void main(String[] args) {
        Properties kafkaProps = Props.getKafkaProps();
        kafkaProps.forEach((k,v)-> System.out.println(k+":"+v));
    }
}
