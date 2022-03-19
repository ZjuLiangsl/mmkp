package org.jeecg.config;

import org.jeecgframework.core.util.ApplicationContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Scott
 * @Date: 2018/2/7
 */

@Configuration
public class AutoPoiConfig {


    @Bean
    public ApplicationContextUtil applicationContextUtil() {
        return new org.jeecgframework.core.util.ApplicationContextUtil();
    }

}
