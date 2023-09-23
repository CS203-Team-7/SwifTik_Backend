package com.swiftyticket.controllers;

import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.dto.auth.JwtAuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.dto.auth.SignUpRequest;
import com.swiftyticket.services.AuthService;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    // We need to invoke the authservice:
    private final AuthService authService;

    // Sign up endpoint:
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Nonnull @Valid @RequestBody SignUpRequest signUpRequest) throws MethodArgumentNotValidException{
        return new ResponseEntity<String>(authService.signup(signUpRequest), HttpStatus.CREATED);
    }

    // Sign in endpoint:
    @PostMapping("/signin")
    public ResponseEntity<JwtAuthResponse> signIn(@Nonnull @Valid @RequestBody SignInRequest signInRequest){
        return new ResponseEntity<JwtAuthResponse>(authService.signIn(signInRequest), HttpStatus.OK);
    }
}
