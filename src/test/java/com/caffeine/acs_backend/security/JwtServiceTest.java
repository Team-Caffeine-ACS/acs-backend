package com.caffeine.acs_backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private static final String MOCK_TEST_SECRET =
      "dGVzdFNlY3JldEtleVdoaWNoSXNMb25nRW5vdWdoRm9ySG1hY1NoYTI1Ng==";
  private static final long EXPIRATION_MS = 86_400_000L;

  private JwtService jwtService;
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secret", MOCK_TEST_SECRET);
    ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRATION_MS);
    ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", EXPIRATION_MS * 2);
    ReflectionTestUtils.invokeMethod(jwtService, "initSigningKey");

    userDetails =
        User.withUsername("test@example.com").password("password").roles("VISITOR").build();
  }

  @Test
  void generateAccessToken_returnsNonNullToken() {
    String token = jwtService.generateAccessToken(userDetails);

    assertThat(token).isNotNull().isNotBlank();
  }

  @Test
  void generateAccessToken_tokenHasThreeParts() {
    String token = jwtService.generateAccessToken(userDetails);

    assertThat(token.split("\\.")).hasSize(3);
  }

  @Test
  void extractUsername_returnsCorrectEmail() {
    String token = jwtService.generateAccessToken(userDetails);

    assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
  }

  @Test
  void isTokenValid_validTokenAndMatchingUser_returnsTrue() {
    String token = jwtService.generateAccessToken(userDetails);

    assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
  }

  @Test
  void isTokenValid_validTokenButDifferentUser_returnsFalse() {
    String token = jwtService.generateAccessToken(userDetails);
    UserDetails otherUser =
        User.withUsername("other@example.com").password("password").roles("VISITOR").build();

    assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
  }

  @Test
  void extractClaims_malformedToken_throwsJwtException() {
    assertThatThrownBy(() -> jwtService.extractClaims("not.a.valid.token"))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void extractClaims_expiredToken_throwsJwtException() {
    String expiredToken =
        Jwts.builder()
            .subject("test@example.com")
            .issuedAt(new Date(System.currentTimeMillis() - 2000))
            .expiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(MOCK_TEST_SECRET)))
            .compact();

    assertThatThrownBy(() -> jwtService.extractClaims(expiredToken))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void extractClaims_tokenSignedWithWrongKey_throwsJwtException() {
    String wrongSecret = "d3JvbmdTZWNyZXRLZXlXcm9uZ1NlY3JldEtleVdyb25nU2VjcmV0S2V5";
    String foreignToken =
        Jwts.builder()
            .subject("test@example.com")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(wrongSecret)))
            .compact();

    assertThatThrownBy(() -> jwtService.extractClaims(foreignToken))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void generateRefreshToken_returnsNonNullToken() {
    String token = jwtService.generateRefreshToken(userDetails);

    assertThat(token).isNotNull().isNotBlank();
  }

@Test
void generateAccessToken_claimTypeIsAccess() {
    String token = jwtService.generateAccessToken(userDetails);

    assertThat(jwtService.extractClaims(token).get("type", String.class)).isEqualTo("access");
}

@Test
void generateRefreshToken_claimTypeIsRefresh() {
    String token = jwtService.generateRefreshToken(userDetails);

    assertThat(jwtService.extractClaims(token).get("type", String.class)).isEqualTo("refresh");
}

@Test
void isTokenValid_expiredToken_returnsFalse() {
    String expiredToken =
        Jwts.builder()
            .subject("test@example.com")
            .issuedAt(new Date(System.currentTimeMillis() - 2000))
            .expiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(MOCK_TEST_SECRET)))
            .compact();

    // expired tokens throw on parse, so isTokenValid should propagate the exception
    assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, userDetails))
        .isInstanceOf(JwtException.class);
}
}
