package com.caffeine.acs_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expirationMs;

  @Value("${jwt.refresh-expiration}")
  private long refreshExpirationMs;

  private SecretKey signingKey;

  @PostConstruct
  private void initSigningKey() {
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String generateAccessToken(UserDetails userDetails) {
    return buildToken(userDetails, expirationMs, "access");
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(userDetails, refreshExpirationMs, "refresh");
  }

  private String buildToken(UserDetails userDetails, long expiration, String type) {
    return Jwts.builder()
        .subject(userDetails.getUsername())
        .claim("type", type)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(signingKey)
        .compact();
  }

  public String extractUsername(String token) {
    return extractClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    Claims claims = extractClaims(token);
    return claims.getSubject().equals(userDetails.getUsername())
        && !claims.getExpiration().before(new Date());
  }

  public Claims extractClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }
}
