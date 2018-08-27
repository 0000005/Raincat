package com.raincat.springcloud.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 此拦截器用于清除threadLocal中的txGroupId
 */
@Configuration
public class SpringCloudMvnInterceptorConfig extends WebMvcConfigurerAdapter {
    /**
     * 该方法用于注册拦截器
     * 可注册多个拦截器，多个拦截器组成一个拦截器链
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("=========================init txTransaction mvn interceptor=========================");
        registry.addInterceptor(new SpringCloudMvnInterceptor())
                .addPathPatterns("/**");
        super.addInterceptors(registry);
    }


}
