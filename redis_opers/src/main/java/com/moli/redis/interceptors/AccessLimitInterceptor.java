package com.moli.redis.interceptors;

import com.google.common.collect.ImmutableList;
import com.moli.redis.annotation.AccessLimit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author moli
 * @time 2024-03-05 11:26:03
 * @description 请求限制拦截器
 */
@Aspect
@Component
public class AccessLimitInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    public static final Logger logger = LoggerFactory.getLogger(AccessLimitInterceptor.class);

    public static final String UNKNOWN = "unknown";

    public AccessLimitInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Pointcut("@annotation(com.moli.redis.annotation.AccessLimit)")
    public void accessLimitJoinPoint() {

    }

    @Around(value = "accessLimitJoinPoint()")
    public Object doIntercept(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        AccessLimit accessLimit = method.getAnnotation(AccessLimit.class);
        AccessLimit.LimitKeyType keyType = accessLimit.keyType();

        String key = null;
        int second = accessLimit.second();
        int count = accessLimit.count();
        String name = accessLimit.name();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        switch (keyType) {
            case CUSTOMER:
                key = accessLimit.key();
                break;
            case IP:
                key = getIpAddr();
                break;
            case METHOD:
            default:
                key = className + "." + method.getName();
                break;
        }

        ImmutableList<String> keys = ImmutableList.of(StringUtils.join(accessLimit.prefix(), key));

        try {
            String script = buildLuaScript();
            RedisScript<Number> redisScript = new DefaultRedisScript<>(script, Number.class);
            Number limit = redisTemplate.execute(redisScript, keys, count, second);
            logger.info("Access try count is {} for name = {} and key = {}", limit, name, key);
            if (limit != null && limit.intValue() <= count) {
                return joinPoint.proceed();
            } else {
                throw new RuntimeException("Access is so quickly");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c");
        lua.append("\nc = redis.call('get',KEYS[1])");
        // 如果redis中存储的value超过最大值，则直接返回
        lua.append("\nif c and tonumber(c) > tonumber(ARGV[1]) then");
        lua.append("\nreturn c;");
        lua.append("\nend");
        // 执行计算器自加
        lua.append("\nc = redis.call('incr',KEYS[1])");
        lua.append("\nif tonumber(c) == 1 then");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("\nredis.call('expire',KEYS[1],ARGV[2])");
        lua.append("\nend");
        lua.append("\nreturn c;");
        return lua.toString();
    }

    public String getIpAddr() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Assert.notNull(attrs, "请求不存在");
        HttpServletRequest request = attrs.getRequest();

        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
