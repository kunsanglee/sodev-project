//package dev.sodev.global.security.config;
//
//import dev.sodev.global.security.interceptor.ReferrerCheckInterceptor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebMvcConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new ReferrerCheckInterceptor()).excludePathPatterns("", "/", "/v1", "/v1/join", "/v1/login", "v1/reissue").addPathPatterns("/**");
//    }
//}
