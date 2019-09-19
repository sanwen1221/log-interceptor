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

        String eventString = new String("10.1.6.44 - [07/Aug/2019:13:59:56 +0800] \"GET /jyx-xxkj/conver/conver!getJyxFileList.action?fileExtName=DOC,XLS,DOCX,XLSX,PDF,TXT&fetchNum=1000 HTTP/1.1\" 200 2 \"-\" 2.083 \"2.083\" \"Apache-HttpClient/4.3.5 (java 1.5)\" \"-\" \"10.1.6.78:9030\" \"xxkj.12study.cn\":\"80\" \"Lang-Code=zh\" log_nginx_k12");
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
