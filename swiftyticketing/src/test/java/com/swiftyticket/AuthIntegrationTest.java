package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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


    
}
