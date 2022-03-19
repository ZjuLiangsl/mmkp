package org.jeecg.config;

import java.io.IOException;

import javax.servlet.*;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;

@Configuration
@AutoConfigureAfter(DruidDataSourceAutoConfigure.class)
public class DruidConfig {

    /**
     *      common.js   ，druid-1.1.14
     */
    private static final String FILE_PATH = "support/http/resources/js/common.js";
    /**
     *     ，
     */
    private static final String ORIGIN_JS = "this.buildFooter();";
    /**
     *
     */
    private static final String NEW_JS = "//this.buildFooter();";

    /**
     *   Druid
     *
     * @param properties DruidStatProperties
     * @return {@link FilterRegistrationBean}
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled", havingValue = "true")
    public FilterRegistrationBean<RemoveAdFilter> removeDruidAdFilter(
            DruidStatProperties properties) throws IOException {
        //   web
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        //   common.js
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        //   common.js
        String text = Utils.readFromResource(FILE_PATH);
        //    this.buildFooter();
        final String newJs = text.replace(ORIGIN_JS, NEW_JS);
        FilterRegistrationBean<RemoveAdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RemoveAdFilter(newJs));
        registration.addUrlPatterns(commonJsPattern);
        return registration;
    }

    /**
     *   druid
     *
     * @author BBF
     */
    private class RemoveAdFilter implements Filter {

        private final String newJs;

        public RemoveAdFilter(String newJS) {
            this.newJs = newJS;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
            //      ，
            response.resetBuffer();
            response.getWriter().write(newJs);
        }
    }
}
