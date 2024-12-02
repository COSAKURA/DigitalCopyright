package com.digitalcopyright.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.AuthenticationException;


import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // 禁用 CSRF 防护
                .authorizeRequests()
                .requestMatchers("/*").permitAll()  // 放行接口
                .anyRequest().authenticated()  // 其他请求需要认证
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler())  // 自定义未认证处理
                .and()
                .formLogin().disable();  // 禁用表单登录（取消默认重定向）

        return http.build();
    }

    /**
     * 自定义未认证处理逻辑，返回 JSON 响应而非重定向
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"error\": \"Unauthorized access\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}
