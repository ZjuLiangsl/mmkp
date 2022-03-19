package org.jeecg.common.util;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import javax.servlet.http.HttpServletRequest;

/**
 * sql
 * 
 * @author zhoujf
 */
@Slf4j
public class SqlInjectionUtil {
	/**
	 * sign           【SQL  】
	 * （      20200501，         ）
	 */
	private final static String TABLE_DICT_SIGN_SALT = "20200501";
	private final static String xssStr = "'|and |exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|or |+";

	/*
	*           sign    （      ）
	* @param dictCode:
	* @param sign:
	* @param request:
	* @Return: void
	*/
	public static void checkDictTableSign(String dictCode, String sign, HttpServletRequest request) {
		//   SQL    ,
		String accessToken = request.getHeader("X-Access-Token");
		String signStr = dictCode + SqlInjectionUtil.TABLE_DICT_SIGN_SALT + accessToken;
		String javaSign = SecureUtil.md5(signStr);
		if (!javaSign.equals(sign)) {
			log.error("   ，SQL           ：" + sign + "!=" + javaSign+ ",dictCode=" + dictCode);
			throw new JeecgBootException("     ！");
		}
		log.info("    ，SQL          ！sign=" + sign + ",dictCode=" + dictCode);
	}


	/**
	 * sql      ，
	 * 
	 * @param value
	 * @return
	 */
	public static void filterContent(String value) {
		if (value == null || "".equals(value)) {
			return;
		}
		//
		value = value.toLowerCase();
		String[] xssArr = xssStr.split("\\|");
		for (int i = 0; i < xssArr.length; i++) {
			if (value.indexOf(xssArr[i]) > -1) {
				log.error("   ，  SQL     ---> {}", xssArr[i]);
				log.error("   ，     SQL    !---> {}", value);
				throw new RuntimeException("   ，     SQL    !--->" + value);
			}
		}
		return;
	}

	/**
	 * sql      ，
	 * 
	 * @param values
	 * @return
	 */
	public static void filterContent(String[] values) {
		String[] xssArr = xssStr.split("\\|");
		for (String value : values) {
			if (value == null || "".equals(value)) {
				return;
			}
			//
			value = value.toLowerCase();
			for (int i = 0; i < xssArr.length; i++) {
				if (value.indexOf(xssArr[i]) > -1) {
					log.error("   ，  SQL     ---> {}", xssArr[i]);
					log.error("   ，     SQL    !---> {}", value);
					throw new RuntimeException("   ，     SQL    !--->" + value);
				}
			}
		}
		return;
	}

	/**
	 * @    (   )        SQL  ，    
	 * @param value
	 * @return
	 */
	@Deprecated
	public static void specialFilterContent(String value) {
		String specialXssStr = " exec | insert | select | delete | update | drop | count | chr | mid | master | truncate | char | declare |;|+|";
		String[] xssArr = specialXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		//       
		value = value.toLowerCase();
		for (int i = 0; i < xssArr.length; i++) {
			if (value.indexOf(xssArr[i]) > -1 || value.startsWith(xssArr[i].trim())) {
				log.error("   ，  SQL     ---> {}", xssArr[i]);
				log.error("   ，     SQL    !---> {}", value);
				throw new RuntimeException("   ，     SQL    !--->" + value);
			}
		}
		return;
	}


    /**
     * @    (   )    Online  SQL  ，    
     * @param value
     * @return
     */
	@Deprecated
	public static void specialFilterContentForOnlineReport(String value) {
		String specialXssStr = " exec | insert | delete | update | drop | chr | mid | master | truncate | char | declare |";
		String[] xssArr = specialXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		//       
		value = value.toLowerCase();
		for (int i = 0; i < xssArr.length; i++) {
			if (value.indexOf(xssArr[i]) > -1 || value.startsWith(xssArr[i].trim())) {
				log.error("   ，  SQL     ---> {}", xssArr[i]);
				log.error("   ，     SQL    !---> {}", value);
				throw new RuntimeException("   ，     SQL    !--->" + value);
			}
		}
		return;
	}

}
