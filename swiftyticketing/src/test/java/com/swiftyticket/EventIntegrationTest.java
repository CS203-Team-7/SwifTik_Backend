package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.swiftyticket.dto.auth.AuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.EventService;
import com.swiftyticket.services.implementations.AuthServiceImpl;
import com.swiftyticket.services.implementations.EventServiceImpl;
import com.swiftyticket.services.implementations.SmsServiceImpl;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j

public class EventIntegrationTest {

    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private EventService eventServ;
    
    @Autowired
    private AuthServiceImpl authServ;

    private String adminToken;
    private String userToken;
    private Event openEvent;
    private Event closedEvent;

    @BeforeEach
    void setUp() throws Exception {
        //create users
        log.info("starting set up");
        String password = "GoodPassword123!";
        String encodedPassowrd = new BCryptPasswordEncoder().encode(password);

        User newUser = new User("newUser@email.com", encodedPassowrd, new Date(), "+6582887066", Role.USER, true);
        userRepo.save(newUser);

        User newAdmin = new User("newAdmin@email.com", encodedPassowrd, new Date(), "+6887662344", Role.ADMIN, true);
        userRepo.save(newAdmin);

        //get their tokens
        AuthResponse adminLogin = authServ.signIn(new SignInRequest("newAdmin@email.com", password));
        adminToken = adminLogin.getToken();

        AuthResponse userLogin = authServ.signIn(new SignInRequest("newUser@email.com", password));
        userToken = userLogin.getToken();
        log.info(""+userToken);
        log.info(""+adminToken);

        //create events
        //create date array for the creation of event.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  
        Date date = sdf.parse("07/03/2023");
        Date[] dates = new Date[]{date};

        //create Artists array
        String[] artists = new String[]{"people","more people"};
        Arrays.asList(artists);

        openEvent = eventServ.addEvent(new Event(port, "concert", Arrays.asList(artists), Arrays.asList(dates), "some stage", 50, false, port, null, null, port));
        //eventRepo.save(openEvent);
        
        closedEvent = eventServ.addEvent(new Event(port, "concert", Arrays.asList(artists), Arrays.asList(dates), "some stage", 50, false, port, null, null, port));
        //eventRepo.save(closedEvent);
    }
    
    // void createUsers(){
    //     String encodedPassowrd = new BCryptPasswordEncoder().encode("GoodPassword123!");

    //     User user = new User("newUser@email.com", encodedPassowrd, new Date(), "+6582887066", Role.USER, true);
    //     userRepo.save(user);

    //     User admin = new User("newAdmin@email.com", encodedPassowrd, new Date(), "+6887662344", Role.ADMIN, true);
    //     userRepo.save(admin);
    // }

    // void createEvents(){
    //     List<String> artists = new ArrayList<>();
    //     artists.add("Taylor Swift");
    //     artists.add("Ed Sheeran");
    //     artists.add("Ariana Grande");
    //     artists.add("BTS");

    //     List<Date> dates = new ArrayList<>();
    //     dates.add(new Date(2021, 10, 10));
    //     dates.add(new Date(2021, 10, 11));
    //     dates.add(new Date(2021, 10, 12));
    //     dates.add(new Date(2021, 10, 13));

    //     Event openEvent = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, true, port, null, null, port);
    //     eventRepo.save(openEvent);

    //     Event closedEvent = new Event(port, "Swifty Concert 2", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
    //     eventRepo.save(closedEvent);
    // }

    
    @AfterEach
    void tearDown(){
        userRepo.deleteAll();
        eventRepo.deleteAll();
    }
    
    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }

    @Test
    public void getEvents_allEventsListed_successful() throws Exception {
        // user login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newUser@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<List<Event>> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events"),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<Event>>() {}
        );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void findEvent_eventWithIdListed_successful() throws Exception {
        // user login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newUser@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}"),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Event.class,
            testEvent.getEventId()
        );

        assertEquals(responseEntity.getBody().getEventId(), testEvent.getEventId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void findEvent_throwEventNotFoundException_failure() throws Exception {
        // user login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newUser@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);
        Integer eventId = testEvent.getEventId();
        eventRepo.delete(testEvent);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}"),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Void.class,
            eventId
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void createEvent_EventCreated_successful() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));
        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");

        HttpEntity<Event> request = new HttpEntity<>(event, headers);
        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/create"),
            HttpMethod.POST,
            request,
            Event.class
        );

        assertEquals(201, responseEntity.getStatusCode().value());
    }

    //################################### POSSIBLY ADD MORE FOR CREATE EVENT? ##############################

    // UPDATE EVENT TESTS (eventupdated success, eventnotfoundexception failure)
    @Test
    public void updateEvent_EventFoundAndUpdated_successful() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);

        Event event2 = new Event(port, "Swifty Concert 2", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}"),
            HttpMethod.PUT,
            new HttpEntity<>(event2, headers),
            Event.class,
            testEvent.getEventId()
        );

        assertEquals(responseEntity.getBody().getEventId(), testEvent.getEventId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateEvent_EventNotFoundException_failure() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);
        Integer eventId = testEvent.getEventId();
        eventRepo.delete(testEvent);

        Event event2 = new Event(port, "Swifty Concert 2", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}"),
            HttpMethod.PUT,
            new HttpEntity<>(event2, headers),
            Void.class,
            eventId
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteEvent_EventFoundAndDeleted_successful() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}"),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class,
            testEvent.getEventId()
        );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteEvent_EventNotFoundException_failure() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);
        Integer eventId = testEvent.getEventId();
        eventRepo.delete(testEvent);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + eventId),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
        // can't get expected 404, keep getting 403 :(
        // ############################################### PLEASE ASSIST ################################################
    }

    // ############################################### DUPLICATED METHOD TO TEST ################################################
    @Test
    public void deleteEvent_EventNotFoundException_failure_v2() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        // List<String> artists = new ArrayList<>();
        // artists.add("Taylor Swift");
        // List<Date> dates = new ArrayList<>();
        // dates.add(new Date(2021, 10, 10));

        // Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        // Event testEvent = eventRepo.saveAndFlush(event);
        // Integer eventId = testEvent.getEventId();
        // eventRepo.delete(testEvent);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            // there is no event with id 999
            createURLWithPort("/events/" + 999),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
        // can't get expected 404, keep getting 403 :(
        // ############################################### PLEASE ASSIST ################################################
    }

    @Test
    public void closeRegistration_EventFoundRegClosed_successful() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, true, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}/close"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class,
            testEvent.getEventId()
        );
        Event updatedEvent = eventRepo.findById(testEvent.getEventId()).orElse(null);

        assertEquals(false, updatedEvent.getOpenStatus());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void closeRegistration_EventNotFoundException_failure() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, true, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);
        Integer eventId = testEvent.getEventId();
        eventRepo.delete(testEvent);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}/close"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            Void.class,
            eventId
        );

        assertEquals(404, responseEntity.getStatusCode().value());
        // why tf am i getting 403???
        // ############################################### PLEASE ASSIST ################################################
    }

    @Test
    public void openRegistration_EventFoundRegOpened_successful() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}/open"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class,
            testEvent.getEventId()
        );
        Event updatedEvent = eventRepo.findById(testEvent.getEventId()).orElse(null);

        assertEquals(true, updatedEvent.getOpenStatus());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void openRegistration_EventNotFoundException_failure() throws Exception {
        // admin login
        SignInRequest loginRequest = new SignInRequest();
        loginRequest.setEmail("newAdmin@email.com");
        loginRequest.setPassword("GoodPassword123!");
        
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Send login request and get jwt token
        HttpEntity<SignInRequest> entity = new HttpEntity<>(loginRequest, authHeaders);
        ResponseEntity<AuthResponse> authResponse = testRestTemplate.exchange(
                createURLWithPort("/auth/signin"),
                HttpMethod.POST, entity, AuthResponse.class
            );
        String jwtToken = authResponse.getBody().getToken();

        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(2021, 10, 10));

        Event event = new Event(port, "Swifty Concert", artists, dates, "Singapore Indoor Stadium", 10000, false, port, null, null, port);
        Event testEvent = eventRepo.saveAndFlush(event);
        Integer eventId = testEvent.getEventId();
        eventRepo.delete(testEvent);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/{id}/open"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            Void.class,
            eventId
        );

        assertEquals(404, responseEntity.getStatusCode().value());
        // why tf am i getting 403???
        // ############################################### PLEASE ASSIST ################################################
    }
}
