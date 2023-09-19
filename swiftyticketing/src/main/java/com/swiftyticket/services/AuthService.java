package com.swiftyticket.services;

import com.swiftyticket.dto.auth.JwtAuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.dto.auth.SignUpRequest;

public interface AuthService {
    String signup(SignUpRequest request);
    JwtAuthResponse signIn(SignInRequest request);
}
