package dev.sodev.global.security.filter;

import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.redis.RedisService;
import dev.sodev.global.security.tokens.JwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // /v1/members/alarms/subscribe?token=eyJhbGciOiJIUzI1NiJ9.eyJzd... 이런 형태로 요청시 url 에 프론트엔드에서 Bearer 떼고 토큰만 담아줘야함. 헤더에 담기지 않기때문.
    private final static List<String> TOKEN_IN_PARAM_URLS = List.of("/v1/members/alarms/subscribe");

    private final AuthenticationManager authenticationManager;
    private final RedisService redisService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, RedisService redisService) {
        this.authenticationManager = authenticationManager;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String token;
        String jwt = resolveToken(request);

        log.info("doFilter 들어옴");

        if (StringUtils.hasText(jwt)) {

            if (redisService.hasKey(jwt)) { // 토큰이 블랙리스트(로그아웃, 회원탈퇴)에 있어서 재로그인 요청
                log.info("블랙리스트에 담긴 토큰 로직 실행");
                String value = (String) redisService.get(jwt);
                if (value.equals("logout")) {
                    log.info("로그아웃한 회원의 토큰 -> 재로그인 요청");
                    throw new SodevApplicationException(ErrorCode.ACCESS_UNAUTHORIZED); // 로그아웃한 토큰 -> 재로그인
                }
            }

            try {
                Authentication jwtAuthenticationToken = new JwtAuthenticationToken(jwt);
                Authentication authentication = authenticationManager.authenticate(jwtAuthenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException authenticationException) {
                SecurityContextHolder.clearContext();
            }
        } else { // alarms/subscribe 요청시 헤더에 토큰이 담기지 않아서 uri 에 토큰 담아보내주는 경우.
            if (TOKEN_IN_PARAM_URLS.contains(request.getRequestURI())) {
                log.info("Request with {} check the query param", request.getRequestURI());
                token = request.getQueryString().split("=")[1].trim();
                log.info("token = {}", token);

                try {
                    Authentication jwtAuthenticationToken = new JwtAuthenticationToken(token);
                    Authentication authentication = authenticationManager.authenticate(jwtAuthenticationToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (AuthenticationException authenticationException) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        return isTokenInParamRequest(bearerToken);
    }

    private static String isTokenInParamRequest(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
