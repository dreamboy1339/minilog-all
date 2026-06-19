package com.asdf.minilog.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.Serializable;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성/파싱/검증 유틸리티.
 *
 * <p>HS256 알고리즘과 Base64로 인코딩된 비밀키({@code jwt.secret})를 사용하여 토큰을 서명/검증한다. 토큰에는 사용자명(subject)과
 * userId 클레임이 담기며, 만료 시간은 {@link #JWT_VALIDITY}(초)로 결정된다.
 */
@Component
public class JwtUtil implements Serializable {

  private static final long serialVersionUID = -2553452335634634564L;
  public static final long JWT_VALIDITY = 60 * 60 * 5;

  @Value("${jwt.secret}")
  private String secret;

  /**
   * 토큰에서 사용자명(subject)을 추출한다.
   *
   * @param token JWT 토큰
   * @return 사용자명
   */
  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  /**
   * 토큰에서 특정 클레임을 추출한다.
   *
   * @param token JWT 토큰
   * @param claimsResolver 추출할 클레임을 선택하는 함수
   * @return 추출된 클레임 값
   */
  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) {
    Key signingKey =
        new SecretKeySpec(
            Base64.getDecoder().decode(secret), SignatureAlgorithm.HS256.getJcaName());
    return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
  }

  /**
   * 토큰에서 userId 클레임을 추출한다. "Bearer " 접두사가 있으면 제거 후 처리한다.
   *
   * @param token JWT 토큰(또는 "Bearer ..." 형태)
   * @return 사용자 ID
   */
  public Long getUserIdFromToken(String token) {
    String jwt;
    if (token.startsWith("Bearer ")) {
      jwt = token.substring(7);
    } else {
      jwt = token;
    }

    return getClaimFromToken(jwt, claims -> claims.get("userId", Long.class));
  }

  /**
   * 토큰의 만료 일시를 추출한다.
   *
   * @param token JWT 토큰
   * @return 만료 일시
   */
  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  // 만료 일시가 현재 시각 이전이면 만료된 것으로 판단
  private Boolean isTokenExpired(String token) {
    Date expirationDate = getExpirationDateFromToken(token);
    return expirationDate.before(new Date());
  }

  /**
   * 사용자 정보로 JWT 토큰을 생성한다.
   *
   * <p>subject에 사용자명, 클레임에 userId를 담고 발급/만료 시각을 설정한 뒤 HS256으로 서명한다.
   *
   * @param userDetails 인증 사용자 정보
   * @param userId 사용자 ID
   * @return 서명된 JWT 문자열
   */
  public String generateToken(UserDetails userDetails, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + JWT_VALIDITY * 1000))
        .signWith(
            new SecretKeySpec(
                Base64.getDecoder().decode(secret), SignatureAlgorithm.HS256.getJcaName()))
        .compact();
  }

  /**
   * 토큰이 해당 사용자에게 유효한지 검증한다.
   *
   * <p>토큰의 사용자명이 일치하고 만료되지 않았을 때만 유효하다.
   *
   * @param token JWT 토큰
   * @param userDetails 비교할 사용자 정보
   * @return 유효하면 {@code true}
   */
  public Boolean validateToken(String token, UserDetails userDetails) {
    String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
