package org.jeecg.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;

/**
 *          
 *            PermissionData ,     request         
 * @Date 2019 4 10 
 * @Version: 1.0
 */
@Aspect
@Component
@Slf4j
public class PermissionDataAspect {

    @Autowired
    private CommonAPI commonAPI;

    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.PermissionData)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object arround(ProceedingJoinPoint point) throws  Throwable{
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        PermissionData pd = method.getAnnotation(PermissionData.class);
        String component = pd.pageComponent();

        String requestMethod = request.getMethod();
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        requestPath = filterUrl(requestPath);
        log.debug("     >> "+requestPath+";     >> "+requestMethod);
        String username = JwtUtil.getUserNameByToken(request);
        //        
        //TODO               
        List<SysPermissionDataRuleModel> dataRules = commonAPI.queryPermissionDataRule(component, requestPath, username);
        if(dataRules!=null && dataRules.size()>0) {
            //    
            JeecgDataAutorUtils.installDataSearchConditon(request, dataRules);
            //TODO               
            SysUserCacheInfo userinfo = commonAPI.getCacheUser(username);
            JeecgDataAutorUtils.installUserInfo(request, userinfo);
        }
        return  point.proceed();
    }

    private String filterUrl(String requestPath){
        String url = "";
        if(oConvertUtils.isNotEmpty(requestPath)){
            url = requestPath.replace("\\", "/");
            url = url.replace("//", "/");
            if(url.indexOf("//")>=0){
                url = filterUrl(url);
            }
			/*if(url.startsWith("/")){
				url=url.substring(1);
			}*/
        }
        return url;
    }

    /**
     *       
     * @param request
     * @return
     */
    private String getJgAuthRequsetPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String requestPath = request.getRequestURI();
        if(oConvertUtils.isNotEmpty(queryString)){
            requestPath += "?" + queryString;
        }
        if (requestPath.indexOf("&") > -1) {//       (      )   ：loginController.do?login
            requestPath = requestPath.substring(0, requestPath.indexOf("&"));
        }
        if(requestPath.indexOf("=")!=-1){
            if(requestPath.indexOf(".do")!=-1){
                requestPath = requestPath.substring(0,requestPath.indexOf(".do")+3);
            }else{
                requestPath = requestPath.substring(0,requestPath.indexOf("?"));
            }
        }
        requestPath = requestPath.substring(request.getContextPath().length() + 1);//       
        return filterUrl(requestPath);
    }

    private boolean moHuContain(List<String> list,String key){
        for(String str : list){
            if(key.contains(str)){
                return true;
            }
        }
        return false;
    }


}
