package com.swiftyticket.services.implementations;

import com.swiftyticket.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    // We need a custom signing - key for the token generation:
    @Value("${token.signing.key}")
    private String jwtSigningKey;

    // This will create the SHA key for the token using our signing key from properties file:
    private Key getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String extractUserName(String token){
        // To actually get to the username from the raw token we need to extract the claims:
        return extractClaim(token, Claims::getSubject);
    }

    // This function is to extract a particular claim from all claims:
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        // This is to specify which particular claims are wanted:
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    // From the raw jwt token we need to extract all the claims from it by using the signing key:
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // This method is to actually create a fresh token:
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername()) // Set any extra claims and set the subject as the user's email
                .setIssuedAt(new Date(System.currentTimeMillis())) // This is the time it was created
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24)) // One day expiration limit for now:
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact(); // This is to encrypt the token with the key we are using
    }

    // This is to check expiration date: so we get the expiration from the claims:
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractExpiration(token).before(new Date());
    }

    // Function to actually extract the expiration date from the token:
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
}
