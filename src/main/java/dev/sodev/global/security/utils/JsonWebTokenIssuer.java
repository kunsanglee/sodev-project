package dev.sodev.global.security.utils;

import dev.sodev.global.security.dto.JsonWebTokenDto;
import dev.sodev.global.security.exception.JwtInvalidException;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;

@Component
public class JsonWebTokenIssuer {

    private final int ONE_SECONDS = 1000;
    private final int ONE_MINUTE = 60 * ONE_SECONDS;
    private final String KEY_ROLES = "roles";

    private final byte[] secretKeyBytes;
    private final byte[] refreshSecretKeyBytes;
    private final int expireMin;
    private final int refreshExpireMin;

    public JsonWebTokenIssuer(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.refresh-secret}") String refreshSecretKey,
            @Value("${jwt.expire-min:30}") int expireMin, // 유효기간 access -> 30분, refresh -> 2주
            @Value("${jwt.refresh-expire-min:10080}") int refreshExpireMin) {
        this.secretKeyBytes = secretKey.getBytes();
        this.refreshSecretKeyBytes = refreshSecretKey.getBytes();
        this.expireMin = expireMin;
        this.refreshExpireMin = refreshExpireMin;
    }

    private String createToken(String memberEmail, String authority, byte[] secretKeyBytes, int expireMin) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(memberEmail);
        claims.put(KEY_ROLES, Collections.singleton(authority));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ONE_MINUTE * expireMin))
                .signWith(SignatureAlgorithm.HS256, secretKeyBytes)
                .compact();
    }

    public long getRefreshExpiration(JsonWebTokenDto tokenDto) {
        Claims refreshClaim = getClaimsFromRefreshToken(tokenDto.refreshToken());
        return refreshClaim.getExpiration().getTime() - new Date().getTime();
    }

    public String createAccessToken(String memberEmail, String authority) {
        return createToken(memberEmail, authority, secretKeyBytes, expireMin);
    }

    public String createRefreshToken(String memberEmail, String authority) {
        return createToken(memberEmail, authority, refreshSecretKeyBytes, refreshExpireMin);
    }

    public Claims getClaimsFromAccessToken(String jsonWebToken) {
        try {
            return Jwts.parser().setSigningKey(secretKeyBytes).parseClaimsJws(jsonWebToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Claims getClaimsFromRefreshToken(String jsonWebToken) {
        try {
            return Jwts.parser().setSigningKey(refreshSecretKeyBytes).parseClaimsJws(jsonWebToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Claims parseClaimsFromRefreshToken(String jsonWebToken) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(refreshSecretKeyBytes).parseClaimsJws(jsonWebToken).getBody();
        } catch (SignatureException signatureException) {
            throw new JwtInvalidException("signature key is different", signatureException);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new JwtInvalidException("expired token", expiredJwtException);
        } catch (MalformedJwtException malformedJwtException) {
            throw new JwtInvalidException("malformed token", malformedJwtException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new JwtInvalidException("using illegal argument like null", illegalArgumentException);
        }
        return claims;
    }
}