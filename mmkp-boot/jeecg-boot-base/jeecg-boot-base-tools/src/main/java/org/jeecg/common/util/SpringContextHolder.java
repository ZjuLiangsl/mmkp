
package org.jeecg.common.util;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @author zyf
 */
@Slf4j
public class SpringContextHolder implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    /**
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // NOSONAR
        SpringContextHolder.applicationContext = applicationContext;
    }

    /**
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     */
    public static <T> T getBean(String name) {
        checkApplicationContext();
        return (T) applicationContext.getBean(name);
    }

    /**
     */
    public static <T> T getHandler(String name, Class<T> cls) {
        T t = null;
        if (ObjectUtil.isNotEmpty(name)) {
            checkApplicationContext();
            try {
                t = applicationContext.getBean(name, cls);
            } catch (Exception e) {
                log.error("####################" + name + "undefined");
            }
        }
        return t;
    }


    /**
     */
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBean(clazz);
    }

    /**
     */
    public static void cleanApplicationContext() {
        applicationContext = null;
    }

    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicaitonContext is not injected. Define SpringContextHolder in ApplicationContext.xml");
        }
    }

}