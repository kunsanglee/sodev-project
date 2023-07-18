package dev.sodev.global.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class ReferrerCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String referer = request.getHeader("Referer");
        String host = request.getHeader("host");
        if (referer == null || !referer.contains(host)) {
            response.sendRedirect("/v1");
            return false;
        }
        return true;
    }
}