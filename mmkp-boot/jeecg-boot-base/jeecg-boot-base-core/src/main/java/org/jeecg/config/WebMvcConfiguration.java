package org.jeecg.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot 2.0
 *
 * @Author qinfeng
 *
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Value("${jeecg.path.upload}")
    private String upLoadPath;
    @Value("${jeecg.path.webapp}")
    private String webAppPath;
    @Value("${spring.resource.static-locations}")
    private String staticLocations;

    /**
     *         -            Html、  、  、
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + upLoadPath + "//", "file:" + webAppPath + "//")
                .addResourceLocations(staticLocations.split(","));
    }

    /**
     *    ：           doc.html   （swagger    ）
     *    ：           index.html   （      ：                webapp，     ）
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("doc.html");
        registry.addViewController("/mmkp").setViewName("index.html");
    }

    @Bean
    @Conditional(CorsFilterCondition.class)
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        //
        corsConfiguration.setAllowCredentials(true);
        //           
        corsConfiguration.addAllowedOrigin("*");
        //               
        corsConfiguration.addAllowedHeader("*");
        //         ,GET POST 
        corsConfiguration.addAllowedMethod("*");
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }


    /**
     *     json ,    long  string
     * js long      
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        //   json      ， java       ，    。
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }

    /**
     * SpringBootAdmin Httptrace   
     * https://blog.csdn.net/u013810234/article/details/110097201
     */
    @Bean
    public InMemoryHttpTraceRepository getInMemoryHttpTrace(){
        return new InMemoryHttpTraceRepository();
    }

}
