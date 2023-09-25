package com.swiftyticket.services;

import com.swiftyticket.dto.auth.AuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.dto.auth.SignUpRequest;

public interface AuthService {
    String signup(SignUpRequest request);
    AuthResponse signIn(SignInRequest request);
}
