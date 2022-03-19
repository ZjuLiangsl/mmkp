package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author scott
 * @Date 2019/9/23 14:12
 * @Description:
 */
@Slf4j
public class TokenUtils {

    /**
     *
     * @param request
     * @return
     */
    public static String getTokenByRequest(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (token == null) {
            token = request.getHeader("X-Access-Token");
        }
        return token;
    }

    /**
     */
    public static boolean verifyToken(HttpServletRequest request, CommonAPI commonAPI, RedisUtil redisUtil) {
        log.debug(" -- url --" + request.getRequestURL());
        String token = getTokenByRequest(request);

        if (StringUtils.isBlank(token)) {
            throw new AuthenticationException("Token cannot be empty!");
        }

        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("Invalid Token!");
        }

        LoginUser user = commonAPI.getUserByName(username);
        if (user == null) {
            throw new AuthenticationException("User does not exist!");
        }
        if (user.getStatus() != 1) {
            throw new AuthenticationException("The account has been locked. Please contact the administrator!");
        }
        if (!jwtTokenRefresh(token, username, user.getPassword(), redisUtil)) {
            throw new AuthenticationException("The Token is invalid. Please log in again");
        }
        return true;
    }

    /**
     * @param token
     * @param userName
     * @param passWord
     * @param redisUtil
     * @return
     */
    private static boolean jwtTokenRefresh(String token, String userName, String passWord, RedisUtil redisUtil) {
        String cacheToken = String.valueOf(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + token));
        if (oConvertUtils.isNotEmpty(cacheToken)) {
            if (!JwtUtil.verify(cacheToken, userName, passWord)) {
                String newAuthorization = JwtUtil.sign(userName, passWord);
                redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, newAuthorization);
                redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME*2 / 1000);
            }
            return true;
        }
        return false;
    }

    /**
     */
    public static boolean verifyToken(String token, CommonAPI commonAPI, RedisUtil redisUtil) {
        if (StringUtils.isBlank(token)) {
            throw new AuthenticationException("Token cannot be empty!");
        }

        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("Invalid token!");
        }

        LoginUser user = commonAPI.getUserByName(username);
        if (user == null) {
            throw new AuthenticationException("User does not exist!");
        }
        if (user.getStatus() != 1) {
            throw new AuthenticationException("The account has been locked. Please contact the administrator!");
        }
        if (!jwtTokenRefresh(token, username, user.getPassword(), redisUtil)) {
            throw new AuthenticationException("The Token is invalid. Please log in again!");
        }
        return true;
    }

}
