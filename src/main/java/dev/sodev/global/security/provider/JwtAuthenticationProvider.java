package dev.sodev.global.security.provider;

import dev.sodev.global.security.exception.JwtInvalidException;
import dev.sodev.global.security.tokens.JwtAuthenticationToken;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final String KEY_ROLES = "roles";
    private final byte[] secretKeyByte;

    public JwtAuthenticationProvider(@Value("${jwt.secret}") String secretKey) {
        this.secretKeyByte = secretKey.getBytes();
    }

    private Collection<? extends GrantedAuthority> createGrantedAuthorities(Claims claims) {
        List<String> roles = (List) claims.get(KEY_ROLES);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String role : roles) {
            grantedAuthorities.add(() -> role);
        }
        return grantedAuthorities;
    }

    /**
     * JwtParser.parse method can throw below exception, so you should catch and do something.
     * MalformedJwtException – if the specified JWT was incorrectly constructed (and therefore invalid). Invalid JWTs should not be trusted and should be discarded.
     * SignatureException – if a JWS signature was discovered, but could not be verified. JWTs that fail signature validation should not be trusted and should be discarded.
     * ExpiredJwtException – if the specified JWT is a Claims JWT and the Claims has an expiration time before the time this method is invoked.
     * IllegalArgumentException – if the specified string is null or empty or only whitespace.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("authenticate 들어옴");
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(secretKeyByte).parseClaimsJws(((JwtAuthenticationToken) authentication).getJsonWebToken()).getBody();
        } catch (SignatureException signatureException) {
            log.info("signatureException");
            throw new JwtInvalidException("signature key is different", signatureException);
        } catch (ExpiredJwtException expiredJwtException) {
            log.info("expiredJwtException");
            throw new JwtInvalidException("expired token", expiredJwtException);
        } catch (MalformedJwtException malformedJwtException) {
            log.info("malformedJwtException");
            throw new JwtInvalidException("malformed token", malformedJwtException);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.info("illegalArgumentException");
            throw new JwtInvalidException("using illegal argument like null", illegalArgumentException);
        }
        return new JwtAuthenticationToken(claims.getSubject(), "", createGrantedAuthorities(claims));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}