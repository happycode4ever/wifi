import com.jj.wifi.flume.dto.interceptor.MailDTO;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DTOTest {
    public static void main(String[] args) throws Exception {
        MailDTO obj = MailDTO.class.newInstance();
        Field[] fields = MailDTO.class.getDeclaredFields();
        for(Field field : fields){
            String name = field.getName();
            System.out.println(name);
            String n = name.substring(0, 1).toUpperCase() + name.substring(1);
            Method method = MailDTO.class.getMethod("set" + n, field.getType());
            method.invoke(obj,"1111");
        }
        System.out.println(obj);
    }
}
