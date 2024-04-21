package cn.tannn.tregistry.core.http;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http请求接口
 *
 * @author tnnn
 * @version V1.0
 * @date 2024-03-20 20:40
 */
public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker Default = new OkHttpInvoker(500);

    String post(String requestString, String url);
    String get(String url);

    @SneakyThrows
    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" ======>>>>>> httpGet: {} " ,  url);
        String respJson = Default.get(url);
        log.debug(" ======>>>>>> response: {} " , respJson);
        return JSON.to(clazz, respJson);
    }

    @SneakyThrows
    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        log.debug(" =====>>>>>> httpGet: {} " ,  url);
        String respJson = Default.get(url);
        log.debug(" =====>>>>>> response: {} " ,  respJson);
        return JSON.parseObject(respJson, typeReference);
    }

    @SneakyThrows
    static <T> T httpPost(String requestString,String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpGet: {} " ,  url);
        String respJson = Default.post(requestString, url);
        log.debug(" =====>>>>>> response: {} " ,  respJson);
        return JSON.parseObject(respJson, clazz);
    }

}
