import com.jj.wifi.props.kafka.SortedProps;
import org.apache.commons.configuration.CompositeConfiguration;

import java.util.Iterator;

public class SortedPropTest {
    public static void main(String[] args) {
        CompositeConfiguration conf = SortedProps.getCompositeConf();
        Iterator iterator = conf.getKeys("wechat");
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
