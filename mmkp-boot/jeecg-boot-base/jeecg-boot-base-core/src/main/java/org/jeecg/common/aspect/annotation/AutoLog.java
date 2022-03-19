package org.jeecg.common.aspect.annotation;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ModuleType;

import java.lang.annotation.*;

/**
 *
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019 1 14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {

	/**
	 *
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 *
	 * 
	 * @return 0:    ;1:    ;2:    ;
	 */
	int logType() default CommonConstant.LOG_TYPE_2;
	
	/**
	 *       
	 * 
	 * @return （1  ，2  ，3  ，4  ）
	 */
	int operateType() default 0;

	/**
	 *         common
	 * @return
	 */
	ModuleType module() default ModuleType.COMMON;
}
