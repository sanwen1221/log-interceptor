import org.apache.flume.Event;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志工具类
 */
public  class LogUtils {


    /**
     * 在事件体的最后添加主题属性
     * 如果是【222.138.64.153 - wangwu [2019-07-21] "GET / HTTP/1.1" 203 5500 "-" "curl/7.29.0" "-"】  来源为ding_test_basic_log
     * 结果为【222.138.64.153 - wangwu [2019-07-21] "GET / HTTP/1.1" 203 5500 "-" "curl/7.29.0" "-" ding_test_basic_log】
     * @param event
     * @return boolean
     */
    public static boolean appendTopic(Event event){

        //获取时间头
        Map<String, String> headers = event.getHeaders();
        //获取主题
        String topic = headers.get("topic");
        //判断事件头topic是否为空
        if (topic==null){
            return false;
        }else{
            //获取事件体
            String log =new String(event.getBody(), Charset.forName("UTF-8"));
            //添加主题字段
            log=log+" "+topic;
            event.setBody(log.getBytes(Charset.forName("UTF-8")));
            return true;
        }

    }

    /**
     * 日志格式化，将不同来源的日志转换成统一格式。每个字段用双引号包含
     * 例如：日志格式【222.138.64.153 - wangwu [2019-07-21] "GET / HTTP/1.1" 203 5500 "-" "curl/7.29.0" "-" ding_test_basic_log】
     * 顺序：【0，1，2，3，4，5，6，7，8，9】
     * 正则表达式 【^([^ ]+) - ([^ ]+) \\[(.*)\\] "([^"]+)" (-|[0-9]+) (-|[0-9]+) "([^"]+)" "([^"]+)" "([^"]+)" (.+)$】
     * 结果为：【222.138.64.153 wangwu 2019-07-21 GET / HTTP/1.1 203 5500 - curl/7.29.0 - ding_test_basic_log】（最后一个字段后面会有一个空格）
     * @param event
     * @return 统一格式的event
     */
    public static Event logFormat(Event event) throws IOException {

        //获取事件体
        String log =new String(event.getBody(), Charset.forName("UTF-8"));
        //按照空格切分，取出最后一列的值即为topic
        String[] logsplits = log.split(" ");
        String topic = logsplits[logsplits.length - 1];//最后一个字段为主题
        //System.out.println("日志来源： "+topic);
        //System.out.println("日志格式为："+log);

        //获取配置文件里面的切分的格式（正则表达式）
        String regex = getProperties("logformat.properties", topic + ".regex");

        Pattern pattern = Pattern.compile(regex);
        //进行切分
        Matcher matcher = pattern.matcher(log);
        matcher.find();

        //获取切分后的组数
        int count = matcher.groupCount();
        //System.out.println("切分后的组数为："+count);

        //获取配置文件字段的顺序
        String fieldsOrder = getProperties("logformat.properties", topic + ".order");
        //将order按照【,】切分
        String[] orders = fieldsOrder.split(",");

        //获取配置文件表的字段数
        Integer fieldsCount = Integer.parseInt(getProperties("logformat.properties", "Hbase_table_field_count"));
        //创建字段数组,将切分好的log按照配置文件的顺序放入数组中
        String[] fieldsArray = new String[fieldsCount];
        //System.out.println("数组长度为："+fieldsArray.length);
        //遍历每一组，将每个字段按照给定顺序放入字段数组中
        for (int i = 0; i < count; i++) {
            //这里是i+1，group（0）是所有匹配的组，每一组是从group（1）开始
           // System.out.println("当前是order"+i+",order结果是："+orders[i]);
            fieldsArray[Integer.parseInt(orders[i])]=matcher.group(i+1);
     //       System.out.println(matcher.group(i+1));
            //stringBuilder.append(matcher.group(i+1)+" ");
            // System.out.println(stringBuilder);
        }

        //创建一个StringBuilder用于生成统一格式的事件体
        StringBuilder stringBuilder = new StringBuilder();

        //遍历fieldsArray，将字段放入到StringBuilder中，空字段直接用逗号隔开，每个字段按照逗号隔开
        for (int i = 0; i < fieldsArray.length; i++) {

            stringBuilder.append("\"").append(fieldsArray[i]).append("\"");


        }

        //System.out.println(stringBuilder);
        //将新的事件体放回事件中
        event.setBody(stringBuilder.toString().getBytes(Charset.forName("UTF-8")));

        return event;

    }

    /**
     * 获取配置文件对应的内容
     * @param name 文件名
     * @param key 对应的属性
     * @return value 对应的值
     * @throws IOException
     */
    public static String getProperties(String name,String key) throws IOException {
        //获取流
        InputStream inputStream = LogUtils.class.getResourceAsStream(name);
        Properties properties = new Properties();
        properties.load(inputStream);

        String value = properties.getProperty(key);

        return value;

    }

}
