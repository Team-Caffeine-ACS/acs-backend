package com.caffeine.acs_backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  @Mock private JwtService jwtService;
  @Mock private UserDetailsService userDetailsService;
  @InjectMocks private JwtAuthFilter filter;

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;
  @Mock private UserDetails userDetails;
  @Mock private Claims claims;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void noAuthorizationHeader_passesThrough_noAuthentication() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void nonBearerHeader_passesThrough_noAuthentication() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void invalidToken_jwtException_passesThrough_noAuthentication() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer bad.token.here");
    when(jwtService.extractClaims("bad.token.here")).thenThrow(new JwtException("invalid"));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(userDetailsService, never()).loadUserByUsername(any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void validToken_setsAuthenticationInSecurityContext() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
    when(jwtService.extractClaims("valid.token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn("user@example.com");
    when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
    when(jwtService.isTokenValid("valid.token", userDetails)).thenReturn(true);
    when(userDetails.getAuthorities()).thenReturn(List.of());

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
        .isEqualTo(userDetails);
  }

  @Test
  void validToken_isTokenValidReturnsFalse_doesNotSetAuthentication() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
    when(jwtService.extractClaims("valid.token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn("user@example.com");
    when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
    when(jwtService.isTokenValid("valid.token", userDetails)).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void alreadyAuthenticated_skipsUserLookup() throws Exception {
    // Pre-populate the security context as if a previous filter already authenticated
    var existingAuth =
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userDetails, null, List.of());
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
    when(jwtService.extractClaims("valid.token")).thenReturn(claims);
    when(claims.getSubject()).thenReturn("user@example.com");

    filter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService, never()).loadUserByUsername(any());
    verify(filterChain).doFilter(request, response);
  }
}
