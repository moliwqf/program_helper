package com.moli.oauth2.resource.security;

import com.nimbusds.jose.JWSObject;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;

/**
 * @author moli
 * @time 2024-04-13 22:48:32
 * @description 用户信息过滤器
 */
@Component
@Order(value = 0)
public class UserCastFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authToken = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authToken)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authToken.replace("Bearer ", "");
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            String user = jwsObject.getPayload().toString();
            System.out.println("Authorization Global Filter cast token -> user = {}" + user);
            request.setAttribute("user", user);
            filterChain.doFilter(request, response);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
