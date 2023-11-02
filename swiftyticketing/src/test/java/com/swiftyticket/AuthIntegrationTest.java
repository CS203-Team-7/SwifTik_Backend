package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.swiftyticket.dto.auth.AuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.dto.auth.SignUpRequest;
import com.swiftyticket.dto.otp.OtpRequest;
import com.swiftyticket.dto.otp.OtpResponseDto;
import com.swiftyticket.dto.otp.OtpStatus;
import com.swiftyticket.dto.otp.OtpValidationRequest;
import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.implementations.SmsServiceImpl;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j

public class AuthIntegrationTest {
    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private SmsServiceImpl smsServ;

    @BeforeEach
    void createUsers(){
        String encodedPassowrd = new BCryptPasswordEncoder().encode("GoodPassword123!");

        User newUser = new User("newUser@email.com", encodedPassowrd, new Date(), "+6582887066", Role.USER, true);
        userRepo.save(newUser);

        User unverifiedUser = new User("notVerifiedUser@email.com", encodedPassowrd, new Date(), "+6582887066", Role.USER, false);
        userRepo.save(unverifiedUser);

        User newAdmin = new User("newAdmin@email.com", encodedPassowrd, new Date(), "+6887662344", Role.ADMIN, true);
        userRepo.save(newAdmin);
    }

    @AfterEach
    void tearDown(){
        userRepo.deleteAll();
    }

    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }

    @Test
    public void login_Valid_ReturnToken() throws Exception {
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newUser@email.com");
        loginRequest.setPassword("GoodPassword123!");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<AuthResponse> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
            
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody().getToken());
    }

    @Test
    public void login_Invalid_ReturnWrongCredentialsException() throws Exception{
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newUser@email.com");
        loginRequest.setPassword("bzzztWrong!");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, Void.class
            );
            
        assertEquals(401, responseEntity.getStatusCode().value());
    }

    @Test
    public void login_Unverified_ReturnAccountNotVerifiedException() throws Exception{
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("notVerifiedUser@email.com");
        loginRequest.setPassword("GoodPassword123!");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, Void.class
            );
            
        assertEquals(401, responseEntity.getStatusCode().value());
    }

    @Test
    public void signup_Valid_ReturnOTPMessage() throws Exception{
        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail("anotherUser@email.com");
        signupRequest.setPassword("GoodPassword123!");
        signupRequest.setDateOfBirth(new Date());
        signupRequest.setPhoneNumber("+6582887066");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<SignUpRequest> entity = new HttpEntity<>(signupRequest, headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/auth/signup"),
                HttpMethod.POST, entity, String.class
            );
        //get user to make sure that user was created successfully
        User createdUser = userRepo.findByEmail("anotherUser@email.com").orElseThrow(() -> new UserNotFoundException());
            
        assertEquals(201, responseEntity.getStatusCode().value());
        assertEquals("Sign up successful, please check your phone for the OTP code.", responseEntity.getBody());
        assertNotNull(createdUser);
        assertFalse(createdUser.isVerified());
        assertEquals(createdUser.getRole(), Role.USER);
    }

    @Test
    public void signup_WeakPassword_Return400() throws Exception{
        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail("anotherUser@email.com");
        signupRequest.setPassword("baaadpassword");
        signupRequest.setDateOfBirth(new Date());
        signupRequest.setPhoneNumber("+6582887066");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<SignUpRequest> entity = new HttpEntity<>(signupRequest, headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/auth/signup"),
                HttpMethod.POST, entity, String.class
            );

        //make sure user wasn't created
        Optional<User> createdUser = userRepo.findByEmail("anotherUser@email.com");
            
        assertEquals(400, responseEntity.getStatusCode().value());
        assertEquals("Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number and one special character", responseEntity.getBody());
        assert(createdUser.isEmpty());
    }

    @Test
    public void signup_DuplicateEmail_Return400() throws Exception{
        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail("newUser@email.com");
        signupRequest.setPassword("GoodPassword123!");
        signupRequest.setDateOfBirth(new Date());
        signupRequest.setPhoneNumber("+6582887066");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<SignUpRequest> entity = new HttpEntity<>(signupRequest, headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/auth/signup"),
                HttpMethod.POST, entity, String.class
            );

        //make sure user wasn't created
        Optional<User> createdUser = userRepo.findByEmail("anotherUser@email.com");
            
        assertEquals(400, responseEntity.getStatusCode().value());
        assertEquals("This Email and/or Phone number is already in use!", responseEntity.getBody());
        assert(createdUser.isEmpty());
    }

    @Test
    public void OTPValidation_Correct_ValidateUser() throws Exception{
        //request for a new OTP 
        smsServ.sendSMS(new OtpRequest("notVerifiedUser@email.com", "+6582887066"));

        //prepare to send request with right OTP for verification.
        OtpValidationRequest req = new OtpValidationRequest();
        req.setEmail("notVerifiedUser@email.com");
        //get the otp that was sent
        String otp = smsServ.getOtpMap().get("notVerifiedUser@email.com");
        
        req.setOtpNumber(otp);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<OtpValidationRequest> entity = new HttpEntity<>(req, headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/otp/validate"),
                HttpMethod.POST, entity, String.class
            );

        //make sure user is now verified.
        User createdUser = userRepo.findByEmail("newUser@email.com").orElseThrow(() -> new UserNotFoundException());
            
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("Success! You may log into your account now", responseEntity.getBody());
        assertTrue(createdUser.isVerified());
    }


    @Test
    public void OTPValidation_Wrong_ReturnMessage() throws Exception{
        OtpValidationRequest req = new OtpValidationRequest();
        req.setEmail("notVerifiedUser@email.com");
        req.setOtpNumber("0");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<OtpValidationRequest> entity = new HttpEntity<>(req, headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/otp/validate"),
                HttpMethod.POST, entity, String.class
            );

        //make sure user's validation still false.
        User createdUser = userRepo.findByEmail("notVerifiedUser@email.com").orElseThrow(() -> new UserNotFoundException());
            
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("OTP is invalid! Please try again, or request for a new OTP", responseEntity.getBody());
        assertFalse(createdUser.isVerified());
    }


    @Test
    public void requestNewOTP_Valid_Return201() throws Exception{
        OtpRequest req = new OtpRequest();
        req.setEmail("newUser@email.com");
        req.setPhoneNumber("+6582887066");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<OtpRequest> entity = new HttpEntity<>(req, headers);
        ResponseEntity<OtpResponseDto> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/otp/send"),
                HttpMethod.POST, entity, OtpResponseDto.class
            );

        //make sure user's validation still false.
        User createdUser = userRepo.findByEmail("notVerifiedUser@email.com").orElseThrow(() -> new UserNotFoundException());
            
        assertEquals(201, responseEntity.getStatusCode().value());
        assertEquals(OtpStatus.DELIVERED, responseEntity.getBody().getStatus());
        assertEquals("OTP sent successfully, please check your phone.", responseEntity.getBody().getMessage());
        assertFalse(createdUser.isVerified());
    }


    @Test
    public void requestNewOTP_Invalid_Return201() throws Exception{
        OtpRequest req = new OtpRequest();
        req.setEmail("nopeUser@email.com");
        req.setPhoneNumber("+6582887066");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");

        HttpEntity<OtpRequest> entity = new HttpEntity<>(req, headers);
        ResponseEntity<OtpResponseDto> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/otp/send"),
                HttpMethod.POST, entity, OtpResponseDto.class
            );

        //make sure user's validation still false.
        User createdUser = userRepo.findByEmail("notVerifiedUser@email.com").orElseThrow(() -> new UserNotFoundException());
            
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(OtpStatus.FAILED, responseEntity.getBody().getStatus());
        assertEquals("Either invalid phone number, or email and phone numbers don't match.", responseEntity.getBody().getMessage());
        assertFalse(createdUser.isVerified());
    }





    
}
