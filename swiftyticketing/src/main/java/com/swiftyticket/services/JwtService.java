package com.swiftyticket.services;

import com.swiftyticket.models.User;

public interface JwtService {
    // We need to know which user is associated with a token:
    String extractUserName(String token);
    // We need to generate a new token everytime a user logs in:
    String generateToken(User user);
    // We also need to check the validity of the token in use:
    boolean isTokenValid(String token);
}
