package com.swiftyticket.services.implementations;

import com.swiftyticket.models.User;
import com.swiftyticket.services.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class JwtServiceImpl implements JwtService {

    // We need a custom signing - key for the token generation:
    @Value("${token.signing.key}")
    private String jwtSigningKey;

    // We make a custom final string for issuer of the token:
    private final static String ISSUER = "SwifTik.com";
    // Logger for debugging:
    private final Logger logger = Logger.getLogger(JwtServiceImpl.class.getName());

    // Method to extract the key from the signing key:
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        JwtBuilder token = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer(ISSUER)
                .setIssuedAt(new Date())
                .setHeaderParam("user_id", user.getUserId())
                .setHeaderParam("role", user.getRole())
                .setExpiration(Date.from(LocalDate.now().plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);
        return token.compact();
    }

    public String extractUserName(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public LocalDateTime getExpirationDate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDateTime();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch(SignatureException e) {
            logger.info("Invalid JWT signature: " + e.getMessage());
        } catch(MalformedJwtException e) {
            logger.info("Invalid JWT token: " + e.getMessage());
        } catch(ExpiredJwtException e) {
            logger.info("JWT token is expired: " + e.getMessage());
        } catch(UnsupportedJwtException e) {
            logger.info("JWT token is unsupported: " + e.getMessage());
        } catch(IllegalArgumentException e) {
            logger.info("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}
