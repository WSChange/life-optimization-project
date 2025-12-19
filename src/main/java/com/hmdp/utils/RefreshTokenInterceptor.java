package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

// 对原有登录拦截的优化，再添加一个拦截器用于拦截所有路径，核心问题是处理token过期的问题
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;// 这里因为是自己创建的类，所以需要构造函数注入对象，而如果是spring创建的对象则可以使用注解注入

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 登录前校验
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1. 获取session
//        HttpSession session = request.getSession();
        // 对1进行优化 --> 获取请求头中的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return true;
        }
        /*
        2. 获取session中的用户
        Object user = session.getAttribute("user");*/
        // 对2优化 --> 基于token获取redis当中的用户
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        //3. 判断用户是否存在
        if (userMap.isEmpty()){
            return true;
        }
        //5，将查询到的hash数据转为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //6. 存在，保存用户信息到ThreadLocal中
        UserHolder.saveUser(userDTO);
        //7. 刷新token有效期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL,TimeUnit.MINUTES);
        //8. 放行
        return true;
    }

    /**
     * 销毁用户信息,避免内存泄漏
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 当前线程执行完毕，需要移除用户
        UserHolder.removeUser();
    }
}
