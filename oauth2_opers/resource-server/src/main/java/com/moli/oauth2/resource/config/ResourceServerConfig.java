package com.moli.oauth2.resource.config;

import com.moli.oauth2.resource.security.AccessDecidedManager;
import com.moli.oauth2.resource.security.FilterMetadataSourceInterceptor;
import com.moli.oauth2.resource.security.UserCastFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.annotation.Resource;

/**
 * @author moli
 * @time 2024-04-12 16:26:50
 * @description 资源服务配置
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Value("${oauth2.check-token-url}")
    private String checkTokenUrl;

    @Value("${oauth2.resource-id}")
    private String resourceId;

    @Value("${oauth2.client-id}")
    private String clientId;

    @Value("${oauth2.client-secret}")
    private String clientSecret;

    @Resource
    private FilterMetadataSourceInterceptor filterMetadataSourceInterceptor;

    @Resource
    private AccessDecidedManager accessDecidedManager;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(resourceId).stateless(true);
        resources.tokenServices(resourceServerTokenServices());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                        object.setAccessDecisionManager(accessDecidedManager);
                        object.setSecurityMetadataSource(filterMetadataSourceInterceptor);
                        return object;
                    }
                })
                .anyRequest()
                .authenticated()
                .and()
                .requestMatchers()
                .antMatchers("/users/**");
    }

    /**
     * 配置 token 校验服务
     * @return
     */
    @Bean
    ResourceServerTokenServices resourceServerTokenServices() {
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        remoteTokenServices.setCheckTokenEndpointUrl(checkTokenUrl);
        remoteTokenServices.setClientId(clientId);
        remoteTokenServices.setClientSecret(clientSecret);
        remoteTokenServices.setAccessTokenConverter(accessTokenConverter());
        return remoteTokenServices;
    }

    @Bean
    public AccessTokenConverter accessTokenConverter() {
        return new DefaultAccessTokenConverter();
    }
}
