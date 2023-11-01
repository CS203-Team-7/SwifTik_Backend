package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.UserRepository;

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

    @BeforeEach
    void createUsers(){
        String encodedPassowrd = new BCryptPasswordEncoder().encode("GoodPassword123!");

        User newUser = new User("newUser@email.com", encodedPassowrd, new Date(), "+6987662344", Role.USER, true);
        userRepo.save(newUser);

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
        loginRequest.setEmail("newUser@email.com");
        loginRequest.setPassword("GoodPassword123!");

        //set the user's verified status to false.
        User newUser = userRepo.findByEmail("newUser@email.com").orElseThrow(() -> new UserNotFoundException());
        newUser.setVerified(false);
        userRepo.save(newUser);
        log.info("" + userRepo.findAll());

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

    
}
