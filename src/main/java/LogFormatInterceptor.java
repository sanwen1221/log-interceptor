import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志格式拦截器
 * 根据日志不同的格式
 */
public class LogFormatInterceptor implements Interceptor {

    private static Logger logger=LoggerFactory.getLogger(LogFormatInterceptor.class);

    @Override
    public void initialize() {

    }

    @Override
    public Event intercept(Event event) {
        Event newEvent=null;
        try {
            //格式化
             newEvent= LogUtils.logFormat(event);
            return newEvent;
        } catch (Exception e) {
            System.err.println("error: 日志为： "+new String(event.getBody(),Charset.forName("UTF-8")));
            e.printStackTrace();
        }finally {
            return newEvent;
        }
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        ArrayList<Event> eventList = new ArrayList<Event>();
        for (Event event : events) {
            Event interceptEvent = intercept(event);
            //不为空则添加到列表中
            if(interceptEvent!=null){
                eventList.add(interceptEvent);
            }
        }

        return eventList;
    }

    @Override
    public void close() {

    }

    public static class Builder implements Interceptor.Builder{

        @Override
        public Interceptor build() {
            return new LogFormatInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }

    public static void main(String[] args) {

        SimpleEvent simpleEvent = new SimpleEvent();

        String eventString = new String("119.137.55.196 - [26/Dec/2019:18:27:37 +0800] \"POST /apis/courseStudyProcess/ksxx HTTP/1.1\" 200 85 \"http://daxue.zhishiditu.net/\" 0.089 \"0.088\" \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36\" \"119.137.55.196\" \"10.1.8.220:8673\" \"daxue.zhishiditu.net\":\"80\" \"p_h5_u=98CF8619-243D-4F34-8F7D-92D7FBBA5124; selectedStreamLevel=LD; SESSION=OTA5MjU5YjItYTgzZi00MmRjLWE4NjQtY2YzMWVkNjdkMWU3\" log_nginx_vocational");
        try {
            simpleEvent.setBody(eventString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        LogFormatInterceptor logFormatInterceptor = new LogFormatInterceptor();

        String newString = new String(logFormatInterceptor.intercept(simpleEvent).getBody(), Charset.forName("UTF-8"));
        System.out.println(newString);


    }
}
