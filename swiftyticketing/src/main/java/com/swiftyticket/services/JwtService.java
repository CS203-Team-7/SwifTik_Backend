package com.swiftyticket.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    // We need to know which user is associated with a token:
    String extractUserName(String token);
    // We need to generate a new token everytime a user logs in:
    String generateToken(UserDetails userDetails);
    // We also need to check the validity of the token in use:
    String isTokenValid(String token, UserDetails userDetails);
}
