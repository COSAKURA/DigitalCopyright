package com.digitalcopyright.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类，用于处理跨域请求 (CORS) 的全局配置。
 *
 * 跨域配置主要解决前端与后端不在同一域名或 IP 下访问时的安全策略限制。
 * 使用本配置后，允许指定的前端地址访问后端 API。
 *
 * 注意：生产环境中应严格限制跨域访问的来源，以提高安全性。
 *
 * @author Sakura
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置全局跨域规则
     *
     * @param registry 跨域配置注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 匹配所有接口
                .allowedOrigins(
                        "http://localhost:8089",         // 本地开发环境
                        "http://bibiyes.xyz",       // 部署的前端地址（使用域名）
                        "http://8.137.114.76"       // 部署的前端地址（使用 IP）
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*")  // 允许所有请求头，提升兼容性
                .allowCredentials(true) // 允许发送 Cookie 或身份凭证
                .maxAge(3600); // 预检请求的缓存时间，单位秒
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源路径映射
        // 例如：将 uploads 目录映射到 /uploads 路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/D:/数字创意作品链上版权认证与交易平台/DigitalCopyright/uploads/");
    }
}
