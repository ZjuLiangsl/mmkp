package org.jeecg.common.system.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Joiner;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;

/**
 * @Author Scott
 * @Date 2018-07-12 14:23
 * @Desc JWT   
 **/
public class JwtUtil {

	// Token    30  （               ， token reids      ）
	public static final long EXPIRE_TIME = 30 * 60 * 1000;

	/**
	 *   token    
	 *
	 * @param token    
	 * @param secret      
	 * @return     
	 */
	public static boolean verify(String token, String username, String secret) {
		try {
			//       JWT   
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
			//   TOKEN
			DecodedJWT jwt = verifier.verify(token);
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	/**
	 *   token      secret      
	 *
	 * @return token       
	 */
	public static String getUsername(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);
			return jwt.getClaim("username").asString();
		} catch (JWTDecodeException e) {
			return null;
		}
	}

	/**
	 *     ,5min   
	 *
	 * @param username    
	 * @param secret        
	 * @return    token
	 */
	public static String sign(String username, String secret) {
		Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
		Algorithm algorithm = Algorithm.HMAC256(secret);
		//   username  
		return JWT.create().withClaim("username", username).withExpiresAt(date).sign(algorithm);

	}

	/**
	 *   request  token      
	 * 
	 * @param request
	 * @return
	 * @throws JeecgBootException
	 */
	public static String getUserNameByToken(HttpServletRequest request) throws JeecgBootException {
		String accessToken = request.getHeader("X-Access-Token");
		String username = getUsername(accessToken);
		if (oConvertUtils.isEmpty(username)) {
			throw new JeecgBootException("      ");
		}
		return username;
	}
	
	/**
	  *   session     
	 * @param key
	 * @return
	 */
	public static String getSessionData(String key) {
		//${myVar}%
		//  ${}     
		String moshi = "";
		if(key.indexOf("}")!=-1){
			 moshi = key.substring(key.indexOf("}")+1);
		}
		String returnValue = null;
		if (key.contains("#{")) {
			key = key.substring(2,key.indexOf("}"));
		}
		if (oConvertUtils.isNotEmpty(key)) {
			HttpSession session = SpringContextUtils.getHttpServletRequest().getSession();
			returnValue = (String) session.getAttribute(key);
		}
		//    ${}     
		if(returnValue!=null){returnValue = returnValue + moshi;}
		return returnValue;
	}
	
	/**
	  *           
	 * @param key
	 * @param user
	 * @return
	 */
	//TODO      sckjkdsjsfjdk
	public static String getUserSystemData(String key,SysUserCacheInfo user) {
		if(user==null) {
			user = JeecgDataAutorUtils.loadUserInfo();
		}
		//#{sys_user_code}%
		
		//         
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		
		String moshi = "";
		if(key.indexOf("}")!=-1){
			 moshi = key.substring(key.indexOf("}")+1);
		}
		String returnValue = null;
		//        #{sysOrgCode}，    
		if (key.contains("#{")) {
			key = key.substring(2,key.indexOf("}"));
		} else {
			key = key;
		}
		//           
		if (key.equals(DataBaseConstant.SYS_USER_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_USER_CODE_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getUsername();
			}else {
				returnValue = user.getSysUserCode();
			}
		}
		//             
		else if (key.equals(DataBaseConstant.SYS_USER_NAME)|| key.toLowerCase().equals(DataBaseConstant.SYS_USER_NAME_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getRealname();
			}else {
				returnValue = user.getSysUserName();
			}
		}
		
		//                 
		else if (key.equals(DataBaseConstant.SYS_ORG_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_ORG_CODE_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getOrgCode();
			}else {
				returnValue = user.getSysOrgCode();
			}
		}
		//                 
		else if (key.equals(DataBaseConstant.SYS_MULTI_ORG_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_MULTI_ORG_CODE_TABLE)) {
			if(user==null){
				//TODO           ，      ，          
				returnValue = sysUser.getOrgCode();
			}else{
				if(user.isOneDepart()) {
					returnValue = user.getSysMultiOrgCode().get(0);
				}else {
					returnValue = Joiner.on(",").join(user.getSysMultiOrgCode());
				}
			}
		}
		//         (   )
		else if (key.equals(DataBaseConstant.SYS_DATE)|| key.toLowerCase().equals(DataBaseConstant.SYS_DATE_TABLE)) {
			returnValue = DateUtils.formatDate();
		}
		//         （      ）
		else if (key.equals(DataBaseConstant.SYS_TIME)|| key.toLowerCase().equals(DataBaseConstant.SYS_TIME_TABLE)) {
			returnValue = DateUtils.now();
		}
		//       （     ）
		else if (key.equals(DataBaseConstant.BPM_STATUS)|| key.toLowerCase().equals(DataBaseConstant.BPM_STATUS_TABLE)) {
			returnValue = "1";
		}
		//update-begin-author:taoyan date:20210330 for:   ID      
		else if (key.equals(DataBaseConstant.TENANT_ID) || key.toLowerCase().equals(DataBaseConstant.TENANT_ID_TABLE)){
			returnValue = sysUser.getRelTenantIds();
			if(oConvertUtils.isEmpty(returnValue) || (returnValue!=null && returnValue.indexOf(",")>0)){
				returnValue = SpringContextUtils.getHttpServletRequest().getHeader(CommonConstant.TENANT_ID);
			}
		}
		//update-end-author:taoyan date:20210330 for:   ID      
		if(returnValue!=null){returnValue = returnValue + moshi;}
		return returnValue;
	}
	
//	public static void main(String[] args) {
//		 String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NjUzMzY1MTMsInVzZXJuYW1lIjoiYWRtaW4ifQ.xjhud_tWCNYBOg_aRlMgOdlZoWFFKB_givNElHNw3X0";
//		 System.out.println(JwtUtil.getUsername(token));
//	}
}
