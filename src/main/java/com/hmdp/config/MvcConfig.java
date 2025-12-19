package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 将自定义拦截器添加到Mvc配置类中使其生效
     * @param registry
     */
    public void addInterceptors(InterceptorRegistry registry) {
        // 这里作为第二个登录拦截器，拦截需要登录才能使用的功能路径
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(//排除不需要拦截的路径
                        "/shop/**",
                        "/shop-type/**",
                        "/voucher/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                ).order(1);
        // 第一个token刷新的拦截器，用于拦截所有路径
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}
