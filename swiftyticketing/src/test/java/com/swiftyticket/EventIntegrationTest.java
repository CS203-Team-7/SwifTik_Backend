package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.swiftyticket.dto.auth.AuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.EventService;
import com.swiftyticket.services.implementations.AuthServiceImpl;
import com.swiftyticket.services.implementations.EventServiceImpl;

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

        openEvent = eventServ.addEvent(new Event(port, "open concert", Arrays.asList(artists), Arrays.asList(dates), "some stage", 50, false, port, null, null, port));
        //eventRepo.save(openEvent);
        
        closedEvent = eventServ.addEvent(new Event(port, "closed concert", Arrays.asList(artists), Arrays.asList(dates), "some stage", 50, false, port, null, null, port));
        //eventRepo.save(closedEvent);
    }

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
        // setup header with userToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + userToken);
        headers.add("Content-Type", "application/json");

        // carry out request with jwt token
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
        // setup header with userToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + userToken);
        headers.add("Content-Type", "application/json");

        // create test event
        Event testEvent = eventRepo.saveAndFlush(openEvent);

        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + testEvent.getEventId()),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Event.class
        );

        assertEquals(responseEntity.getBody().getEventId(), testEvent.getEventId());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void findEvent_throwEventNotFoundException_failure() throws Exception {
        // setup header with userToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + userToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            // there is no event with id 999
            createURLWithPort("/events/" + 999),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void createEvent_EventCreated_successful() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        // create test event
        Event testEvent = eventRepo.saveAndFlush(openEvent);

        HttpEntity<Event> request = new HttpEntity<>(testEvent, headers);
        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/create"),
            HttpMethod.POST,
            request,
            Event.class
        );

        assertEquals(201, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateEvent_EventFoundAndUpdated_successful() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        // create test event
        Event testEventBefore = eventRepo.saveAndFlush(openEvent);
        Integer eventId = testEventBefore.getEventId();
        Event testEventAfter = eventRepo.saveAndFlush(closedEvent);

        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + eventId),
            HttpMethod.PUT,
            new HttpEntity<>(testEventAfter, headers),
            Event.class
        );

        assertEquals(responseEntity.getBody().getEventId(), eventId);
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void updateEvent_EventNotFoundException_failure() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        // create test event
        Event testEvent = eventRepo.saveAndFlush(closedEvent);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            // there is no event with id 999
            createURLWithPort("/events/" + 999),
            HttpMethod.PUT,
            new HttpEntity<>(testEvent, headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteEvent_EventFoundAndDeleted_successful() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        // create test event
        Event testEvent = eventRepo.saveAndFlush(openEvent);
        Integer eventId = testEvent.getEventId();

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + eventId),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteEvent_EventNotFoundException_failure() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            // there is no event with id 999
            createURLWithPort("/events/" + 999),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void closeRegistration_EventFoundRegClosed_successful() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        // create test event
        Event testOpenEvent = eventRepo.saveAndFlush(openEvent);
        Integer eventId = testOpenEvent.getEventId();

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + eventId +"/close"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );
        Event updatedEvent = eventRepo.findById(eventId).orElse(null);

        assertFalse(updatedEvent.getOpenStatus());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void closeRegistration_EventNotFoundException_failure() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            // there is no event with id 999
            createURLWithPort("/events/" + 999 +"/close"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void openRegistration_EventFoundRegOpened_successful() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        // create test event
        Event testClosedEvent = eventRepo.saveAndFlush(closedEvent);
        Integer eventId = testClosedEvent.getEventId();

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + eventId +"/open"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );
        Event updatedEvent = eventRepo.findById(eventId).orElse(null);

        assertTrue(updatedEvent.getOpenStatus());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void openRegistration_EventNotFoundException_failure() throws Exception {
        // setup header with adminToken
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            // there is no event with id 999
            createURLWithPort("/events/" + 999 +"/open"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }
}
