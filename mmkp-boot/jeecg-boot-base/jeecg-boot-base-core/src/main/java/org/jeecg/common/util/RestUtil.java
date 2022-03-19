package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 *    Restful    Util
 *
 * @author sunjianlei
 */
@Slf4j
public class RestUtil {

    private static String domain = null;

    public static String getDomain() {
        if (domain == null) {
            domain = SpringContextUtils.getDomain();
            // issues/2959
            //               
            //            ，   SpringContextUtils.getDomain()               :-1      ，      -1       。
            if (domain.endsWith(":-1")) {
                domain = domain.substring(0, domain.length() - 3);
            }
        }
        return domain;
    }

    public static String path = null;

    public static String getPath() {
        if (path == null) {
            path = SpringContextUtils.getApplicationContext().getEnvironment().getProperty("server.servlet.context-path");
        }
        return oConvertUtils.getString(path);
    }

    public static String getBaseUrl() {
        String basepath = getDomain() + getPath();
        log.info(" RestUtil.getBaseUrl: " + basepath);
        return basepath;
    }

    /**
     * RestAPI    
     */
    private final static RestTemplate RT;

    static {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        RT = new RestTemplate(requestFactory);
        //       
        RT.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public static RestTemplate getRestTemplate() {
        return RT;
    }

    /**
     *    get   
     */
    public static JSONObject get(String url) {
        return getNative(url, null, null).getBody();
    }

    /**
     *    get   
     */
    public static JSONObject get(String url, JSONObject variables) {
        return getNative(url, variables, null).getBody();
    }

    /**
     *    get   
     */
    public static JSONObject get(String url, JSONObject variables, JSONObject params) {
        return getNative(url, variables, params).getBody();
    }

    /**
     *    get   ，     ResponseEntity   
     */
    public static ResponseEntity<JSONObject> getNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.GET, variables, params);
    }

    /**
     *    Post   
     */
    public static JSONObject post(String url) {
        return postNative(url, null, null).getBody();
    }

    /**
     *    Post   
     */
    public static JSONObject post(String url, JSONObject params) {
        return postNative(url, null, params).getBody();
    }

    /**
     *    Post   
     */
    public static JSONObject post(String url, JSONObject variables, JSONObject params) {
        return postNative(url, variables, params).getBody();
    }

    /**
     *    POST   ，     ResponseEntity   
     */
    public static ResponseEntity<JSONObject> postNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.POST, variables, params);
    }

    /**
     *    put   
     */
    public static JSONObject put(String url) {
        return putNative(url, null, null).getBody();
    }

    /**
     *    put   
     */
    public static JSONObject put(String url, JSONObject params) {
        return putNative(url, null, params).getBody();
    }

    /**
     *    put   
     */
    public static JSONObject put(String url, JSONObject variables, JSONObject params) {
        return putNative(url, variables, params).getBody();
    }

    /**
     *    put   ，     ResponseEntity   
     */
    public static ResponseEntity<JSONObject> putNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.PUT, variables, params);
    }

    /**
     *    delete   
     */
    public static JSONObject delete(String url) {
        return deleteNative(url, null, null).getBody();
    }

    /**
     *    delete   
     */
    public static JSONObject delete(String url, JSONObject variables, JSONObject params) {
        return deleteNative(url, variables, params).getBody();
    }

    /**
     *    delete   ，     ResponseEntity   
     */
    public static ResponseEntity<JSONObject> deleteNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.DELETE, null, variables, params, JSONObject.class);
    }

    /**
     *     
     */
    public static ResponseEntity<JSONObject> request(String url, HttpMethod method, JSONObject variables, JSONObject params) {
        return request(url, method, getHeaderApplicationJson(), variables, params, JSONObject.class);
    }

    /**
     *     
     *
     * @param url              
     * @param method           
     * @param headers             
     * @param variables      url     
     * @param params         body     
     * @param responseType     
     * @return ResponseEntity<responseType>
     */
    public static <T> ResponseEntity<T> request(String url, HttpMethod method, HttpHeaders headers, JSONObject variables, Object params, Class<T> responseType) {
        log.info(" RestUtil  --- request ---  url = "+ url);
        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("url     ");
        }
        if (method == null) {
            throw new RuntimeException("method     ");
        }
        if (headers == null) {
            headers = new HttpHeaders();
        }
        //    
        String body = "";
        if (params != null) {
            if (params instanceof JSONObject) {
                body = ((JSONObject) params).toJSONString();

            } else {
                body = params.toString();
            }
        }
        //    url   
        if (variables != null) {
            url += ("?" + asUrlVariables(variables));
        }
        //     
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return RT.exchange(url, method, request, responseType);
    }

    /**
     *   JSON   
     */
    public static HttpHeaders getHeaderApplicationJson() {
        return getHeader(MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    /**
     *      
     */
    public static HttpHeaders getHeader(String mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        headers.add("Accept", mediaType);
        return headers;
    }

    /**
     *   JSONObject    a=1&b=2&c=3...&n=n    
     */
    public static String asUrlVariables(JSONObject variables) {
        Map<String, Object> source = variables.getInnerMap();
        Iterator<String> it = source.keySet().iterator();
        StringBuilder urlVariables = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            String value = "";
            Object object = source.get(key);
            if (object != null) {
                if (!StringUtils.isEmpty(object.toString())) {
                    value = object.toString();
                }
            }
            urlVariables.append("&").append(key).append("=").append(value);
        }
        //      &
        return urlVariables.substring(1);
    }

}
