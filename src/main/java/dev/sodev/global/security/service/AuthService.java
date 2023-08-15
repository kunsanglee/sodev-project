package dev.sodev.global.security.service;

import dev.sodev.domain.alarm.repository.AlarmRepository;
import dev.sodev.domain.comment.repsitory.CommentRepository;
import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.follow.repository.FollowRepository;
import dev.sodev.domain.likes.repository.LikeRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.dto.MemberWithdrawal;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.domain.member.repository.MemberProjectRepository;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.redis.CacheName;
import dev.sodev.global.redis.RedisService;
import dev.sodev.global.security.dto.JsonWebTokenDto;
import dev.sodev.global.security.exception.JwtInvalidException;
import dev.sodev.global.security.utils.JsonWebTokenIssuer;
import dev.sodev.global.security.utils.SecurityUtil;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class AuthService {

    private final String GRANT_TYPE_BEARER = "Bearer";
    private final String CACHE_NAME_PREFIX = CacheName.MEMBER + "::";
    private final long TWO_WEEKS = 1000 * 60 * 60 * 12 * 14;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JsonWebTokenIssuer jwtIssuer;
    private final RedisService redisService;
    private final ProjectRepository projectRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final AlarmRepository alarmRepository;
    private final EntityManager em;


    // 로그인
    public JsonWebTokenDto login(MemberLoginRequest request) {
        String email = request.email();
        if (isCachedTokenValid(email)) {
            return getCachedToken(email);
        }

        Member member = getMemberByEmail(email);
        validatePassword(request.password(), member);

        JsonWebTokenDto tokenDto = issueToken(member);
        cacheToken(member.getEmail(), tokenDto);

        return tokenDto;
    }

    // 토큰 재발급
    public JsonWebTokenDto reissue(String refreshToken) {
        Claims claims = parseAndValidRefreshToken(refreshToken);
        Member member = getMemberByEmail(claims.getSubject());

        JsonWebTokenDto jsonWebTokenDto = createJsonWebTokenDto(member);
        cacheToken(member.getEmail(), jsonWebTokenDto);

        return jsonWebTokenDto;
    }

    // 로그아웃
    public void logout(String accessToken, String refreshToken) {
        Claims claims = parseAndValidRefreshToken(refreshToken);
        String resolvedAccessToken = resolveToken(accessToken);
        String email = claims.getSubject();

        deleteCache(email);
        cacheTokenLogout(refreshToken, claims, resolvedAccessToken);
    }

    // 회원탈퇴
    public void withdrawal(MemberWithdrawal memberWithdrawal, String accessToken, String refreshToken) {
        String memberEmail = SecurityUtil.getMemberEmail();
        Member member = getMemberWithComments(memberEmail);
        validatePassword(memberWithdrawal.checkPassword(), member);
        parseAndValidRefreshToken(refreshToken);
        String resolvedAccessToken = resolveToken(accessToken);

        deleteCache(member.getEmail());
        cacheTokenWithdrawal(refreshToken, resolvedAccessToken);
        bulkDeleteMemberData(member);

        memberRepository.delete(member); // 회원 삭제
    }

    // 토큰 파싱.
    private String resolveToken(String accessToken) {
        if (StringUtils.hasText(accessToken) && accessToken.startsWith(GRANT_TYPE_BEARER)) {
            return accessToken.substring(7);
        }
        throw new SodevApplicationException(ErrorCode.INVALID_TOKEN);
    }

    // 토큰 생성.
    private JsonWebTokenDto createJsonWebTokenDto(Member member) {
        String memberEmail = member.getEmail();
        Auth authority = member.getAuth();
        return JsonWebTokenDto.builder()
                .grantType(GRANT_TYPE_BEARER)
                .accessToken(jwtIssuer.createAccessToken(memberEmail, authority.getKey()))
                .refreshToken(jwtIssuer.createRefreshToken(memberEmail, authority.getKey()))
                .build();
    }

    // 이메일로 회원 조회.
    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("username is not found"));
    }

    // 탈퇴시 댓글 fetch join 하여 조회.
    private Member getMemberWithComments(String memberEmail) {
        return memberRepository.findByEmailWithComments(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // redis 에 캐싱된 토큰이 유효한지 확인.
    private boolean isCachedTokenValid(String email) {
        if (!redisService.hasKey(CACHE_NAME_PREFIX + email)) {
            return false;
        }

        JsonWebTokenDto tokenDto = (JsonWebTokenDto) redisService.get(CACHE_NAME_PREFIX + email);
        Claims accessTokenClaims = jwtIssuer.getClaimsFromAccessToken(tokenDto.accessToken());
        Claims refreshTokenClaims = jwtIssuer.getClaimsFromRefreshToken(tokenDto.refreshToken());

        boolean isAccessTokenValid = accessTokenClaims.getExpiration().after(new Date());
        boolean isRefreshTokenValid = refreshTokenClaims.getExpiration().after(new Date());

        if (!isAccessTokenValid && isRefreshTokenValid) {
            tokenDto = reissue(tokenDto.refreshToken());
            cacheToken(email, tokenDto);
        }

        return isAccessTokenValid || isRefreshTokenValid;
    }

    // redis 에 캐싱된 토큰 있는지 확인.
    private JsonWebTokenDto getCachedToken(String email) {
        return (JsonWebTokenDto) redisService.get(CACHE_NAME_PREFIX + email);
    }

    // 비밀번호 검증.
    private void validatePassword(String password, Member member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("bad credential: using unmatched password");
        }
    }

    // 토큰 발급.
    private JsonWebTokenDto issueToken(Member member) {
        return createJsonWebTokenDto(member);
    }

    // redis 에 토큰 캐싱.
    private void cacheToken(String email, JsonWebTokenDto tokenDto) {
        long expiration = jwtIssuer.getRefreshExpiration(tokenDto);
        redisService.set(CACHE_NAME_PREFIX + email, tokenDto, expiration);
    }

    // refresh 토큰 파싱, 유효성 검사.
    private Claims parseAndValidRefreshToken(String refreshToken) {
        Claims claims = jwtIssuer.parseClaimsFromRefreshToken(refreshToken);
        if (claims == null) {
            throw new JwtInvalidException("not exists claims in token");
        }
        return claims;
    }

    // redis 에 캐싱된 데이터 삭제
    private void deleteCache(String email) {
        redisService.delete(CACHE_NAME_PREFIX + email);
    }

    // 로그아웃시 1주간 해당 유저의 accessToken, refreshToken 을 블랙리스트에 추가하고 추후 해당 토큰으로 요청시 에러 반환.
    private void cacheTokenLogout(String refreshToken, Claims claims, String resolvedAccessToken) {
        redisService.set(resolvedAccessToken, "logout", (claims.getExpiration().getTime() - new Date().getTime()));
        redisService.set(refreshToken, "logout", (claims.getExpiration().getTime() - new Date().getTime()));
    }

    // 탈퇴 회원과 연관되어있는 데이터(follow, like, memberProject, comment, project, alarm) 벌크 삭제
    private void bulkDeleteMemberData(Member member) {
        followRepository.deleteAllByMemberId(member.getId());
        likeRepository.deleteAllByMemberId(member.getId());
        memberProjectRepository.deleteAllByMemberId(member.getId());
        member.removeAllComments();
        List<Project> projectList = projectRepository.findAllByCreatedBy(member.getEmail());
        projectList.forEach(p -> commentRepository.deleteAllByProjectId(p.getId()));
        projectRepository.deleteAllByCreatedBy(member.getEmail()); // 이메일로 프로젝트 찾아서 삭제.
        alarmRepository.deleteAllByMemberId(member.getId());
        em.flush();
        em.clear();
    }

    // redis 에 탈퇴 회원 토큰 캐싱.
    private void cacheTokenWithdrawal(String refreshToken, String resolvedAccessToken) {
        redisService.set(resolvedAccessToken, "withdrawal", TWO_WEEKS);
        redisService.set(refreshToken, "withdrawal", TWO_WEEKS);
    }
}