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

    /**
     * Returns the signing key for the JWT token.
     * @return Key -> Signing key for the JWT token
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a unique JWT token for the user with the specified expiry date.
     * @param user -> User object containing the user's details
     * @return String token -> JWT token for the user
     */
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

    /**
     * Extracts the user's email from the JWT token.
     * @param token -> String JWT token
     * @return String email -> User's email
     */
    public String extractUserName(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Extracts the expiry date from the JWT token. This is to help validate the token.
     * @param token -> String JWT token
     * @return LocalDateTime expiry date -> Expiry date of the JWT token
     */
    public LocalDateTime getExpirationDate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Validates the JWT token by checking certain requirements.
     * @param token -> String JWT token
     * @throws SignatureException -> if the JWT token signature is invalid
     * @throws MalformedJwtException -> if the JWT token is malformed
     * @throws ExpiredJwtException -> if the JWT token is expired
     * @throws UnsupportedJwtException -> if the JWT token is unsupported
     * @throws IllegalArgumentException -> if the JWT claims string is empty
     * @throws NullPointerException -> if the JWT token is null
     * @throws IllegalArgumentException -> if the JWT token is empty
     * @return boolean isValid -> true if the JWT token is valid, false otherwise
     */
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
