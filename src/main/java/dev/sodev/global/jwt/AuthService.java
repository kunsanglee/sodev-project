package dev.sodev.global.jwt;

import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.redis.CacheName;
import dev.sodev.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisService redisService;

    private final String SERVER = "Server";

    // 로그인: 인증 정보 저장 및 Bearer 토큰 발급
    @Transactional
    @Cacheable(value = CacheName.MEMBER, key = "#request.email()", unless = "#result == null")
    public TokenDto login(MemberLoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());

        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return generateToken(SERVER, authentication.getName(), getAuthorities(authentication));
    }

    // 로그아웃
    @Transactional
    @CacheEvict(value = CacheName.MEMBER, key = "#email")
    public void logout(String accessToken, String refreshToken, String email) {

        // 해당 유저의 accessToken, refreshToken 을 블랙리스트에 추가하고 추후 해당 토큰으로 요청시 에러 반환.
        redisService.setBlackList(accessToken.substring(7), "accessToken", 1800);
        redisService.setBlackList(refreshToken.substring(14), "refreshToken",60400);

    }

    // AT가 만료일자만 초과한 유효한 토큰인지 검사
    public boolean validate(String requestAccessTokenInHeader) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);
        return jwtTokenProvider.validateAccessTokenOnlyExpired(requestAccessToken); // true = 재발급
    }

    // 토큰 재발급: validate 메서드가 true 반환할 때만 사용 -> AT, RT 재발급
    @Transactional
    public TokenDto reissue(String requestAccessTokenInHeader, String requestRefreshToken) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);

        Authentication authentication = jwtTokenProvider.getAuthentication(requestAccessToken);
        String principal = getPrincipal(requestAccessToken);

        String refreshTokenInRedis = redisService.getValues(CacheName.MEMBER + "::" + principal);
        if (refreshTokenInRedis == null) { // Redis에 저장되어 있는 RT가 없을 경우
            throw new SodevApplicationException(ErrorCode.ACCESS_UNAUTHORIZED); // -> 재로그인 요청
        }

        // 요청된 RT의 유효성 검사 & Redis에 저장되어 있는 RT와 같은지 비교
        if(!jwtTokenProvider.validateToken(requestRefreshToken) || !refreshTokenInRedis.equals(requestRefreshToken)) {
            redisService.deleteValues(CacheName.MEMBER + "::" + principal); // 탈취 가능성 -> 삭제
            throw new SodevApplicationException(ErrorCode.ACCESS_UNAUTHORIZED); // -> 재로그인 요청
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String authorities = getAuthorities(authentication);

        // 토큰 재발급 및 Redis 업데이트
        redisService.deleteValues(CacheName.MEMBER + "::" + principal); // 기존 RT 삭제
        TokenDto tokenDto = jwtTokenProvider.createToken(principal, authorities);
        saveRefreshToken(SERVER, principal, tokenDto.refreshToken());
        return tokenDto;
    }

    // 토큰 발급
    @Transactional
    public TokenDto generateToken(String provider, String email, String authorities) {
        // RT가 이미 있을 경우
        if(redisService.getValues("login::" + email) != null) {
            redisService.deleteValues("login::" + email); // 삭제
        }

        // AT, RT 생성 및 Redis에 RT 저장
        TokenDto tokenDto = jwtTokenProvider.createToken(email, authorities);
        saveRefreshToken(provider, email, tokenDto.refreshToken());
        return tokenDto;
    }

    // RT를 Redis에 저장
    @Transactional
    public void saveRefreshToken(String provider, String principal, String refreshToken) {
        redisService.setValuesWithTimeout(CacheName.MEMBER + "::" + principal, refreshToken, jwtTokenProvider.getTokenExpirationTime(refreshToken));
    }

    // 권한 이름 가져오기
    public String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    // AT로부터 principal 추출
    public String getPrincipal(String requestAccessToken) {
        return jwtTokenProvider.getAuthentication(requestAccessToken).getName();
    }

    // "Bearer {AT}"에서 {AT} 추출
    public String resolveToken(String requestAccessTokenInHeader) {
        if (requestAccessTokenInHeader != null && requestAccessTokenInHeader.startsWith("Bearer ")) {
            return requestAccessTokenInHeader.substring(7);
        }
        throw new SodevApplicationException(ErrorCode.INVALID_TOKEN);
    }
}