
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * 日志拦截器
 * 根据前一个静态拦截器增加的主题名
 * 在每一条日志的最后增加一个数据来源字段
 * 例如：日志log.hearder是higher，则增加的字段也是higher
 */
public class LogInterceptor implements Interceptor {

    private static Logger logger=LoggerFactory.getLogger(LogInterceptor.class);

    public void initialize() {
        //不做处理
    }

    public Event intercept(Event event) {

        boolean result = false;
        try {
            result = LogUtils.appendTopic(event);
        } catch (Exception e) {
            System.err.println("error:"+new String(event.getBody(),Charset.forName("UTF-8")));
            e.printStackTrace();
        }

        if(result){
            return event;
        }else {
            return null;
        }

    }

    public List<Event> intercept(List<Event> list) {

        ArrayList<Event> events = new ArrayList<Event>();

        for (Event event : list) {
            Event intercept = intercept(event);
            //不为空则添加到events中
            if(intercept!=null){
                events.add(event);
            }
        }
        return events;
    }

    public void close() {

    }

    public static class Builder implements Interceptor.Builder{

        @Override
        public Interceptor build() {
            return new LogInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
