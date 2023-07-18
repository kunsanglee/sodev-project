package dev.sodev.global.security.filter;

import dev.sodev.global.redis.RedisService;
import dev.sodev.global.security.exception.JwtInvalidException;
import dev.sodev.global.security.tokens.JwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    MockHttpServletRequest mockRequest;
    MockHttpServletResponse mockResponse;
    FilterChain mockFilterChain;
    AuthenticationManager mockAuthenticationmanager;
    RedisService redisService;

    JwtAuthenticationFilter filter;

    @BeforeEach
    public void setup() {
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
        mockFilterChain = Mockito.mock(FilterChain.class);
        mockAuthenticationmanager = Mockito.mock(AuthenticationManager.class);
        redisService = Mockito.mock(RedisService.class);
        filter = new JwtAuthenticationFilter(mockAuthenticationmanager, redisService);
    }

    @Test
    public void givenTokenNotInHeader_whenDoFilterInternal_thenAuthenticationManagerNotBeenCalled() throws Exception {
        // given
        when(mockAuthenticationmanager.authenticate(any())).thenReturn(null);

        // when
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockAuthenticationmanager, never()).authenticate(any());
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void givenInvalidTokenInHeader_whenDoFilterInternal_thenAuthenticationManagerNotBeenCalled() throws Exception {
        // given
        mockRequest.addHeader("Authorization", "invalid token");
        when(mockAuthenticationmanager.authenticate(any())).thenReturn(null);

        // when
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockAuthenticationmanager, never()).authenticate(any());
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void givenReturnNullAfterAuthenticateWithValidToken_whenDoFilterInternal_thenAuthenticationFromSecurityContextHolderIsNull() throws Exception {
        // given
        mockRequest.addHeader("Authorization", "Bearer valid_token");
        JwtAuthenticationToken token = new JwtAuthenticationToken("valid_token");

        when(mockAuthenticationmanager.authenticate(token)).thenReturn(null);
        // when
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());
    }

    @Test
    public void givenThrowAuthenticationException_whenDoFilterInternal_thenSecurityContextInContextHolderIsNullAndClearContextBeenCalled() throws Exception {
        // given
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        MockedStatic<SecurityContextHolder> utilities = mockStatic(SecurityContextHolder.class);

        utilities.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        mockRequest.addHeader("Authorization", "Bearer valid_token");
        JwtAuthenticationToken token = new JwtAuthenticationToken("valid_token");

        when(mockAuthenticationmanager.authenticate(token)).thenThrow(new JwtInvalidException("time expired"));

        // when
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // then
        utilities.verify(SecurityContextHolder::clearContext, times(1));
        assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());

        // clear static Mockito
        Mockito.clearAllCaches();
    }

    @Test
    public void givenValidToken_whenDoFilterInternal_thenSecurityContextHasAuthentication() throws Exception {
        // given
        mockRequest.addHeader("Authorization", "Bearer valid_token");
        JwtAuthenticationToken token = new JwtAuthenticationToken("valid_token");
        JwtAuthenticationToken authenticatedToken = new JwtAuthenticationToken(
                "Lee",
                "",
                Collections.singletonList(
                        () -> "ROLE_ADMIN"
                )
        );

        when(mockAuthenticationmanager.authenticate(token)).thenReturn(authenticatedToken);

        // when
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication(), equalTo(authenticatedToken));
    }

}