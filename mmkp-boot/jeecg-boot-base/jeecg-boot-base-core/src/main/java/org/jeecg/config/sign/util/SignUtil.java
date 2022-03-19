package org.jeecg.config.sign.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.StaticConfig;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.SortedMap;

/**
 *      
 * 
 * @author jeecg
 * @date 20210621
 */
@Slf4j
public class SignUtil {
    public static final String xPathVariable = "x-path-variable";

    /**
     * @param params
     *                              
     * @return       
     */
    public static boolean verifySign(SortedMap<String, String> params,String headerSign) {
        if (params == null || StringUtils.isEmpty(headerSign)) {
            return false;
        }
        //      
        String paramsSign = getParamsSign(params);
        log.info("Param Sign : {}", paramsSign);
        return !StringUtils.isEmpty(paramsSign) && headerSign.equals(paramsSign);
    }

    /**
     * @param params
     *                              
     * @return     
     */
    public static String getParamsSign(SortedMap<String, String> params) {
        //   Url      
        params.remove("_t");
        String paramsJsonStr = JSONObject.toJSONString(params);
        log.info("Param paramsJsonStr : {}", paramsJsonStr);
        StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
        String signatureSecret = staticConfig.getSignatureSecret();
        if(oConvertUtils.isEmpty(signatureSecret) || signatureSecret.contains("${")){
            throw new JeecgBootException("     ${jeecg.signatureSecret}      ！！");
        }
        return DigestUtils.md5DigestAsHex((paramsJsonStr + signatureSecret).getBytes()).toUpperCase();
    }
}