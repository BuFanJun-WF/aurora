package com.aurora.config;

import com.aurora.filter.JwtAuthenticationTokenFilter;
import com.aurora.handler.AccessDecisionManagerImpl;
import com.aurora.handler.FilterInvocationSecurityMetadataSourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 用于提供请求URL的安全元数据，从而决定是否需要进行安全拦截和访问控制，负责获取某个请求对应的安全元数据，如该请求所需的角色、权限等信息
     * @return 返回对应实现类
     */
    @Bean
    public FilterInvocationSecurityMetadataSource securityMetadataSource() {
        return new FilterInvocationSecurityMetadataSourceImpl();
    }

    /**
     * 作用是根据用户的安全身份和请求所需的权限，决定用户是否具有访问请求资源的权限
     * @return 返回对应实现类
     */
    @Bean
    public AccessDecisionManager accessDecisionManager() {
        return new AccessDecisionManagerImpl();
    }

    /**
     * 作用是根据用户提供的凭据进行身份验证,负责验证用户的用户名和密码是否有效，并返回一个经过身份认证的Authentication对象。
     * @return 返回对应实现类
     * @throws Exception 异常
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 返回数据库密码加密bean
     * @return 数据密码加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 用户登陆
        http.formLogin()
                // 指定表单提交地址
                .loginProcessingUrl("/users/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler);
        // 配置请求权限控制
        http.authorizeRequests()
                // 使用ObjectPostProcessor对FilterSecurityInterceptor进行后置处理
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O fsi) {
                        // 设置安全元数据源为securityMetadataSource()方法返回的对象。安全元数据源负责从数据库、配置文件等读取资源与角色/权限的对应关系
                        fsi.setSecurityMetadataSource(securityMetadataSource());
                        // 设置访问决策管理器为accessDecisionManager()方法返回的对象。访问决策管理器根据用户的角色/权限与请求的资源权限进行比对，决定是否允许访问。
                        fsi.setAccessDecisionManager(accessDecisionManager());
                        return fsi;
                    }
                })
                // 配置任意请求都允许访问，即没有进行具体的权限控制的请求都被允许访问。
                .anyRequest().permitAll()
                .and()
                // 配置CSRF（跨站请求伪造）保护关闭，并指定异常处理相关的配置。
                .csrf().disable().exceptionHandling()
                // 指定身份认证入口点，用于处理未经身份验证的请求。
                .authenticationEntryPoint(authenticationEntryPoint)
                // 指定访问拒绝处理器，用于处理权限不足的请求。
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                // 配置会话管理和会话创建策略为无状态。这意味着不会创建或使用会话来存储用户的身份信息，每个请求都需要重新进行身份验证。
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 在校验用户名密码前，校验jwt
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
