package com.swiftyticket.services.implementations;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.swiftyticket.dto.auth.JwtAuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.dto.auth.SignUpRequest;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.AuthService;
import com.swiftyticket.services.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public JwtAuthResponse signup(SignUpRequest request) {
        // Creating a new user in the DB and making a JWT Token for them:
        var user = User.builder().dateOfBirth(request.getDateOfBirth()).phoneNumber(request.getPhoneNumber())
                        .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                        .role(Role.USER).verified(false).build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return JwtAuthResponse.builder().token(jwtToken).build();
    }

    @Override
    public JwtAuthResponse signIn(SignInRequest request) {
        // First we check if the username and password actually match:
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        // Once authenticated: create JWT Token and then send response
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        var jwtToken = jwtService.generateToken(user);
        return JwtAuthResponse.builder().token(jwtToken).build();
    }
}
