package com.taotao.cloud.auth.config;

import com.taotao.cloud.auth.properties.SecurityProperties;
import com.taotao.cloud.common.constant.ProjectNameConstant;
import com.taotao.cloud.common.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.annotation.Resource;

/**
 * oauth2资源服务器
 *
 * @author dengtao
 * @date 2020/4/30 09:04
 */
@Slf4j
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter implements InitializingBean {

    @Autowired
    private TokenStore tokenStore;

    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Resource
    private OAuth2WebSecurityExpressionHandler expressionHandler;

    @Resource
    private OAuth2AccessDeniedHandler oAuth2AccessDeniedHandler;

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void afterPropertiesSet() {
        LogUtils.info(ResourceServerConfig.class, ProjectNameConstant.TAOTAO_CLOUD_AUTH_STARTER, "资源服务器已启动");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl = setHttp(http)
                .authorizeRequests()
                .antMatchers(securityProperties.getIgnore().getUrls()).permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest();
        setAuthenticate(authorizedUrl);

        http.formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/user/login").permitAll();
        http.logout().permitAll();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .httpBasic().disable()
                .headers()
                .frameOptions().disable()
                .and()
                .csrf().disable();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(tokenStore)
                .stateless(true)
                .authenticationEntryPoint(authenticationEntryPoint)
                .expressionHandler(expressionHandler)
                .accessDeniedHandler(oAuth2AccessDeniedHandler);
    }

    /**
     * 留给子类重写扩展功能
     *
     * @param http
     */
    public HttpSecurity setHttp(HttpSecurity http) {
        return http;
    }

    /**
     * url权限控制，默认是认证就通过，可以重写实现个性化
     *
     * @param authorizedUrl
     */
    public HttpSecurity setAuthenticate(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl) {
        return authorizedUrl.authenticated().and();
    }
}
