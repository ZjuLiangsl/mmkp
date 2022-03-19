package org.jeecg.config.sign.interceptor;


import java.io.PrintWriter;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.DateUtils;
import org.jeecg.config.sign.util.BodyReaderHttpServletRequestWrapper;
import org.jeecg.config.sign.util.HttpUtils;
import org.jeecg.config.sign.util.SignUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 *      
 * @author qinfeng
 */
@Slf4j
public class SignAuthInterceptor implements HandlerInterceptor {
    /**
     * 5     
     */
    private final static long MAX_EXPIRE = 5 * 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("request URI = " + request.getRequestURI());
        HttpServletRequest requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
        //      (  URL body  )
        SortedMap<String, String> allParams = HttpUtils.getAllParams(requestWrapper);
        //         
        String headerSign = request.getHeader(CommonConstant.X_SIGN);
        String timesTamp = request.getHeader(CommonConstant.X_TIMESTAMP);

        //1.       
        try {
            DateUtils.parseDate(timesTamp, "yyyyMMddHHmmss");
        } catch (Exception e) {
            throw new IllegalArgumentException("      :X-TIMESTAMP     :yyyyMMddHHmmss");
        }
        Long clientTimestamp = Long.parseLong(timesTamp);
        //      timestamp=201808091113
        if ((DateUtils.getCurrentTimestamp() - clientTimestamp) > MAX_EXPIRE) {
            throw new IllegalArgumentException("      :X-TIMESTAMP   ");
        }

        //2.    
        boolean isSigned = SignUtil.verifySign(allParams,headerSign);

        if (isSigned) {
            log.debug("Sign     ！Header Sign : {}",headerSign);
            return true;
        } else {
            log.error("request URI = " + request.getRequestURI());
            log.error("Sign       ！Header Sign : {}",headerSign);
            //        
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            Result<?> result = Result.error("Sign      ！");
            out.print(JSON.toJSON(result));
            return false;
        }
    }

}
