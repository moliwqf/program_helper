package com.moli.oauth2.auth.config;

import com.moli.oauth2.auth.domain.SecurityUser;
import com.moli.oauth2.auth.security.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author moli
 * @time 2024-04-11 23:10:40
 * @description 认证服务配置类
 */
@Configuration
@EnableAuthorizationServer
public class Oauth2ServerConfig extends AuthorizationServerConfigurerAdapter {

    private final DataSource dataSource;

    private final PasswordEncoder passwordEncoder;

    private final UserDetailServiceImpl userDetailService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private TokenStore tokenStore;

    @Autowired
    public Oauth2ServerConfig(DataSource dataSource, PasswordEncoder passwordEncoder, UserDetailServiceImpl userDetailService) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
        this.userDetailService = userDetailService;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                // 允许表单认证
                .allowFormAuthenticationForClients()
                // 开放 /oauth/token_key 获取 token 加密公钥
                .tokenKeyAccess("permitAll()")
                // 开放 /oauth/check_token
                .checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> delegates = new ArrayList<>();

        delegates.add(tokenEnhancer());
        delegates.add(accessTokenConverter());

        // 配置 JWT 内容增强
        enhancerChain.setTokenEnhancers(delegates);

        endpoints
                // 开启密码模式授权
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailService)
                .accessTokenConverter(accessTokenConverter())
                .tokenStore(tokenStore)
                .tokenEnhancer(enhancerChain)
                .tokenServices(defaultTokenServices());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("oauth-server")
                .secret(passwordEncoder.encode("auth"))
                .authorizedGrantTypes("password", "refresh_token", "authorization_code")
                .scopes("all")
                .accessTokenValiditySeconds(60 * 1000 * 60)
                .refreshTokenValiditySeconds(60 * 1000 * 60);
        /*// 使用基于 JDBC 存储模式
        JdbcClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
        // client_secret 加密
        clientDetailsService.setPasswordEncoder(passwordEncoder);
        clients.withClientDetails(clientDetailsService);*/
    }

    @Primary
    @Bean
    public DefaultTokenServices defaultTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore);
        tokenServices.setSupportRefreshToken(true);
//        tokenServices.setClientDetailsService(customClientDetailsService);
        // token有效期自定义设置，90天
        tokenServices.setAccessTokenValiditySeconds(60 * 60 * 24);
        // refresh_token 90天
        tokenServices.setRefreshTokenValiditySeconds(60 * 60 * 24);
        return tokenServices;
    }

    /**
     * token 转换器
     * 默认是 uuid 格式，我们在这里指定 token 格式为 jwt
     * 使用非对称加密算法对 token 签名
     *
     * @return
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        // 使用非对称加密算法对 token 签名
        converter.setKeyPair(keyPair());
        return converter;
    }

    @Bean
    public KeyPair keyPair() {
        // 从 classpath 目录下的证书 jwt.jks 中获取秘钥对，输入在以上生成密钥对设置的密码: 123456，这里使用硬编码，建议写到配置文件中
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), "123456".toCharArray());
        return keyStoreKeyFactory.getKeyPair("moli", "123456".toCharArray());
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return (oAuth2AccessToken, oAuth2Authentication) -> {
            Map<String, Object> map = new HashMap<>(1);
            SecurityUser userDTO = (SecurityUser) oAuth2Authentication.getPrincipal();
            map.put("userName", userDTO.getUsername());
            // TODO 其他信息可以自行添加
            ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(map);
            return oAuth2AccessToken;
        };
    }
}
