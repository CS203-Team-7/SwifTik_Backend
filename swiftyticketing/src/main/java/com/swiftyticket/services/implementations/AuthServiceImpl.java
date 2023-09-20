package com.swiftyticket.services.implementations;

import com.swiftyticket.exceptions.IncorrectUserPasswordException;
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
import com.swiftyticket.dto.otp.OtpRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SmsServiceImpl smsServ;

    @Override
    public String signup(SignUpRequest request) {
        // Creating a new user in the DB and making a JWT Token for them:
        var user = User.builder().dateOfBirth(request.getDateOfBirth()).phoneNumber(request.getPhoneNumber())
                        .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                        .role(Role.USER).verified(false).build();
        userRepository.save(user);

        // Create OTP request object to send the SMS
        OtpRequest otpReq = new OtpRequest( request.getEmail(), request.getPhoneNumber() );
        smsServ.sendSMS(otpReq);


        return "Sign up successful, please check your phone for the OTP code.";
    }

    @Override
    public JwtAuthResponse signIn(SignInRequest request) throws IncorrectUserPasswordException {
        // First we check if the username and password actually match:
        try {
            authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception e){
            throw new IncorrectUserPasswordException("Incorrect email or password");
        }
        
        // Once authenticated: create JWT Token and then send response
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        var jwtToken = jwtService.generateToken(user);
        return JwtAuthResponse.builder().token(jwtToken).build();
    }
}
