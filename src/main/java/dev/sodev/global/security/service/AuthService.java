package dev.sodev.global.security.service;

import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.dto.MemberWithdrawal;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.redis.CacheName;
import dev.sodev.global.redis.RedisService;
import dev.sodev.global.security.dto.JsonWebTokenDto;
import dev.sodev.global.security.exception.JwtInvalidException;
import dev.sodev.global.security.utils.JsonWebTokenIssuer;
import dev.sodev.global.security.utils.SecurityUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final String GRANT_TYPE_BEARER = "Bearer";
    private final String CACHE_NAME_PREFIX = CacheName.MEMBER + "::";
    private final long TWO_WEEKS = 1000 * 60 * 60 * 12 * 14;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JsonWebTokenIssuer jwtIssuer;
    private final RedisService redisService;


    private String resolveToken(String accessToken) {
        if (StringUtils.hasText(accessToken) && accessToken.startsWith(GRANT_TYPE_BEARER)) {
            return accessToken.substring(7);
        }
        return null;
    }

    private JsonWebTokenDto createJsonWebTokenDto(Member member) {
        String memberEmail = member.getEmail();
        Auth authority = member.getAuth();
        return JsonWebTokenDto.builder()
                .grantType(GRANT_TYPE_BEARER)
                .accessToken(jwtIssuer.createAccessToken(memberEmail, authority.getKey()))
                .refreshToken(jwtIssuer.createRefreshToken(memberEmail, authority.getKey()))
                .build();
    }

    public JsonWebTokenDto login(MemberLoginRequest request) {

        // 캐싱된게 남아있으면 accessToken 유효기간 검증
        if (redisService.hasKey(CACHE_NAME_PREFIX + request.email())) {
            log.info("캐시 남아있어서 들어옴");
            JsonWebTokenDto tokenDto = (JsonWebTokenDto) redisService.get(CACHE_NAME_PREFIX + request.email());
            Claims access = jwtIssuer.getClaimsFromAccessToken(tokenDto.accessToken());
            if (access.getExpiration().after(new Date())) {
                log.info("login 에서 redis 캐싱된 토큰 반환");
                return tokenDto;
            } else {
                Claims refresh = jwtIssuer.getClaimsFromRefreshToken(tokenDto.refreshToken());
                if (refresh.getExpiration().after(new Date())) {
                    log.info("login 에서 reissue");
                    // 만약 accessToken 은 만료, refreshToken 은 살아있다 -> silent refresh 사용자는 모르게 그냥 재발급
                    return reissue(tokenDto.refreshToken());
                }
            }
            // accessToken, refreshToken 유효기간 모두 만료 -> 재로그인
            log.info("accessToken, refreshToken 모두 만료. 다시 로그인");
        }

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("username is not found"));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BadCredentialsException("bad credential: using unmatched password");
        }

        JsonWebTokenDto jsonWebTokenDto = createJsonWebTokenDto(member);
        long expiration = jwtIssuer.getRefreshExpiration(jsonWebTokenDto);
        log.info("정상회원 토큰 발급 + redis 에 토큰 저장");
        redisService.set(CACHE_NAME_PREFIX + member.getEmail(), jsonWebTokenDto, (expiration));

        return jsonWebTokenDto;
    }

    public JsonWebTokenDto reissue(String refreshToken) {

//        if (!StringUtils.hasText(refreshToken)) {
//            throw new JwtInvalidException("invalid grant type");
//        }

        Claims claims = jwtIssuer.parseClaimsFromRefreshToken(refreshToken);
        if (claims == null) {
            throw new JwtInvalidException("not exists claims in token");
        }

        Member member = memberRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new UsernameNotFoundException("username is not found"));

        JsonWebTokenDto jsonWebTokenDto = createJsonWebTokenDto(member);
        long expiration = jwtIssuer.getRefreshExpiration(jsonWebTokenDto);
        redisService.set(CACHE_NAME_PREFIX + member.getEmail(), jsonWebTokenDto, expiration);

        return jsonWebTokenDto;
    }

    // 로그아웃
    public void logout(String accessToken, String refreshToken) {

        Claims claims = jwtIssuer.parseClaimsFromRefreshToken(refreshToken);
        if (claims == null) {
            throw new JwtInvalidException("not exists claims in token");
        }

        String resolvedAccessToken = resolveToken(accessToken);

        // redis 에 캐싱된 데이터 삭제
        log.info("로그아웃 -> redis 에 캐싱된 로그인 데이터 삭제={}", claims.getSubject());
        redisService.delete(CACHE_NAME_PREFIX + claims.getSubject());

        // 로그아웃시 1주간 해당 유저의 accessToken, refreshToken 을 블랙리스트에 추가하고 추후 해당 토큰으로 요청시 에러 반환.
        log.info("로그아웃한 access, refresh 토큰 logout 등록");
        redisService.set(resolvedAccessToken, "logout", (claims.getExpiration().getTime() - new Date().getTime()));
        redisService.set(refreshToken, "logout", (claims.getExpiration().getTime() - new Date().getTime()));

    }

    // 회원탈퇴
    public void withdrawal(MemberWithdrawal memberWithdrawal, String accessToken, String refreshToken) {
        log.info("회원탈퇴 메서드 시작");

        String memberEmail = SecurityUtil.getMemberEmail();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(memberWithdrawal.checkPassword(), member.getPassword())) {
            log.info("회원탈퇴 -> 비밀번호 오류.");
            throw new SodevApplicationException(ErrorCode.INVALID_PASSWORD);
        }

        String resolvedAccessToken = resolveToken(accessToken);
        if (resolvedAccessToken == null) {
            log.info("유효하지 않은 토큰 -> 재로그인 요청");
            throw new SodevApplicationException(ErrorCode.INVALID_TOKEN);
        }

        log.info("회원 탈퇴 -> redis 에 캐싱된 로그인 데이터 삭제={}", member.getEmail());
        redisService.delete(CACHE_NAME_PREFIX + member.getEmail()); // 회원 로그인 캐시 삭제
        redisService.set(resolvedAccessToken, "withdrawal", TWO_WEEKS);
        redisService.set(refreshToken, "withdrawal", TWO_WEEKS);
        log.info("회원 탈퇴 완료={}", member.getEmail());
    }
}