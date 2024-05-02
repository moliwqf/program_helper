package com.moli.oauth2.resource.security;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;

/**
 * @author moli
 * @time 2024-04-13 22:00:47
 * @description 获取权限
 */
@Component
public class FilterMetadataSourceInterceptor implements FilterInvocationSecurityMetadataSource {

    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        FilterInvocation fi = (FilterInvocation) o;
        String method = fi.getRequest().getMethod();
        String url = fi.getRequest().getRequestURI();

        AntPathMatcher antPathMatcher = new AntPathMatcher();
        if (antPathMatcher.match("/users/**", url)) {
            return SecurityConfig.createList("admin");
        } else {
            return SecurityConfig.createList("disable");
        }
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
