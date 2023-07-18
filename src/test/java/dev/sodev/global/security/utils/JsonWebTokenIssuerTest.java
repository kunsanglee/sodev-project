package dev.sodev.global.security.utils;

import dev.sodev.domain.enums.Auth;
import dev.sodev.global.security.exception.JwtInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

public class JsonWebTokenIssuerTest {

    JsonWebTokenIssuer jsonWebTokenIssuer;

    @BeforeEach
    public void setup() {
        jsonWebTokenIssuer = new JsonWebTokenIssuer(
                "secretKeysecretKeysecretKeysecretKeysecretKeysecret",
                "refreshSecretKeyrefreshSecretKeyrefreshSecretKeyrefreshSecretKeyrefreshSecretKey",
                10,
                30);
    }

    Claims parseClaims(String jsonWebToken, String secretKey) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(jsonWebToken)
                .getBody();
    }

    @Test
    public void givenUser_whenCreateAccessTokenByUser_thenParsedClaimsWithSameValue() {

        String jwt = jsonWebTokenIssuer.createAccessToken("lee@naver.com", Auth.ADMIN.getKey());

        Claims claims = parseClaims(jwt, "secretKeysecretKeysecretKeysecretKeysecretKeysecret");

        assertThat(claims.getSubject(), equalTo("lee@naver.com"));
        assertThat(claims.get("roles"), isA(List.class));
        List<String> roles = (List) claims.get("roles");
        for (String role : roles) {
            assertThat(role, equalTo("ROLE_ADMIN"));
        }
    }

    @Test
    public void givenUser_whenCreateRefreshTokenByUser_thenParsedClaimsWithSameValue() {

        String jwt = jsonWebTokenIssuer.createRefreshToken("lee@naver.com", Auth.ADMIN.getKey());

        Claims claims = parseClaims(jwt, "refreshSecretKeyrefreshSecretKeyrefreshSecretKeyrefreshSecretKeyrefreshSecretKey");

        assertThat(claims.getSubject(), equalTo("lee@naver.com"));
        assertThat(claims.get("roles"), isA(List.class));
        List<String> roles = (List) claims.get("roles");
        for (String role : roles) {
            assertThat(role, equalTo("ROLE_ADMIN"));
        }
    }

    @Test
    public void givenInValidRefreshToken_whenParseClaimsFromRefreshToken_thenThrowJwtInvalidException() {

        String invalidRefreshToken = "invalid refresh token";

        assertThrows(JwtInvalidException.class, () -> {
            jsonWebTokenIssuer.parseClaimsFromRefreshToken(invalidRefreshToken);
        });
    }

    @Test
    public void givenAccessToken_whenParseClaimsFromRefreshToken_thenThrowsJwtInvalidException() {

        String accessToken = jsonWebTokenIssuer.createAccessToken("lee@naver.com", Auth.ADMIN.getKey());

        assertThrows(JwtInvalidException.class, () -> {
            jsonWebTokenIssuer.parseClaimsFromRefreshToken(accessToken);
        });
    }

    @Test
    public void givenRefreshToken_whenParseClaimsFromRefreshToken_thenReturnClaims() {

        String refreshToken = jsonWebTokenIssuer.createRefreshToken("lee@naver.com", Auth.ADMIN.getKey());

        Claims claims = jsonWebTokenIssuer.parseClaimsFromRefreshToken(refreshToken);

        assertThat(claims.getSubject(), equalTo("lee@naver.com"));
        assertThat(claims.get("roles"), isA(List.class));
        List<String> roles = (List) claims.get("roles");
        for (String role : roles) {
            assertThat(role, equalTo("ROLE_ADMIN"));
        }
    }
}