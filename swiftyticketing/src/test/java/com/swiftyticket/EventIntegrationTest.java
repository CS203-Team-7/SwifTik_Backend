package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import com.swiftyticket.dto.auth.AuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.services.implementations.AuthServiceImpl;

import lombok.extern.slf4j.Slf4j;

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
    private AuthServiceImpl authServ;

    private String userToken;
    private String adminToken;
    Event openEvent;
    Event closedEvent;

    @BeforeEach
    void setup() throws Exception{
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

        //create events.
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");
        artists.add("Ed Sheeran");
        artists.add("Ariana Grande");
        artists.add("BTS");

        //create date array for the creation of event.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  
        Date date = sdf.parse("07/03/2023");
        Date[] dates = new Date[]{date};

        openEvent = new Event("Swifty Concert", artists, Arrays.asList(dates), "Singapore Indoor Stadium", 10000);
        openEvent.setOpen4Registration(true);
        eventRepo.save(openEvent);

        closedEvent = new Event("Swifty Concert 2 Electric Boogalo", artists, Arrays.asList(dates), "Singapore Indoor Stadium", 10000);
        eventRepo.save(closedEvent);
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

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + userToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<List<Event>> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events"),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<Event>>() {}
        );

        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void findEvent_eventWithIdListed_successful() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + userToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + openEvent.getEventId()),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Event.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(responseEntity.getBody(), openEvent);
    }

    @Test
    public void findEvent_throwEventNotFoundException_failure() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + userToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            //negative id will never be generated, so this should not exist.
            createURLWithPort("/events/-1"),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void createEvent_EventCreated_successful() throws Exception {
        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");

        //create date array for the creation of event.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  
        Date date = sdf.parse("07/03/2023");
        Date[] dates = new Date[]{date};

        Event testEvent = new Event("test Swifty Concert", artists, Arrays.asList(dates), "Singapore Indoor Stadium", 10000);

        // carry out request with (admin's) jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        HttpEntity<Event> request = new HttpEntity<>(testEvent, headers);
        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/create"),
            HttpMethod.POST,
            request,
            Event.class
        );

        assertEquals(201, responseEntity.getStatusCode().value());
    }


    // UPDATE EVENT TESTS (eventupdated success, eventnotfoundexception failure)
    //only admin should be able to do these.
    @Test
    public void updateEvent_EventFoundAndUpdated_successful() throws Exception {
        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");

        //create date array for the creation of event.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  
        Date date = sdf.parse("07/03/2023");
        Date[] dates = new Date[]{date};

        Event testEvent = new Event("test Swifty Concert", artists, Arrays.asList(dates), "Singapore Indoor Stadium", 10000);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        //we will update openEvent and change it into testEvent.
        ResponseEntity<Event> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + openEvent.getEventId()),
            HttpMethod.PUT,
            new HttpEntity<>(testEvent, headers),
            Event.class,
            testEvent.getEventId()
        );

        assertEquals(200, responseEntity.getStatusCode().value());
        //ensure ID stay the same
        assertEquals(responseEntity.getBody().getEventId(), openEvent.getEventId());
    }

    @Test
    public void updateEvent_EventNotFoundException_failure() throws Exception {
        // create test event
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");

        //create date array for the creation of event.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  
        Date date = sdf.parse("07/03/2023");
        Date[] dates = new Date[]{date};

        Event testEvent = new Event("test Swifty Concert", artists, Arrays.asList(dates), "Singapore Indoor Stadium", 10000);

        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        //we will attempt to update openEvent and change it into testEvent.
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            //we will never have an event with a negative id, so will not be found, should throw exception here
            createURLWithPort("/events/-1"),
            HttpMethod.PUT,
            new HttpEntity<>(testEvent, headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteEvent_EventFoundAndDeleted_successful() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        //we will attempt to delete openEvent
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + openEvent.getEventId()),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void deleteEvent_EventNotFoundException_failure() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");


        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            //we will never have an event with negative id
            createURLWithPort("/events/-1"),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }


    @Test
    public void closeRegistration_EventFoundRegClosed_successful() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + openEvent.getEventId() +"/close"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );
    
        //check that its openstatus became false.
        Event updatedEvent = eventRepo.findById(openEvent.getEventId()).orElse(null);

        assertEquals(false, updatedEvent.getOpenStatus());
        assertEquals(200, responseEntity.getStatusCode().value());
    }

    @Test
    public void closeRegistration_EventNotFoundException_failure() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/-1/close"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            Void.class
        );
        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void openRegistration_EventFoundRegOpened_successful() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        //we will attempt to open the closed event.
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/"+ closedEvent.getEventId() +"/open"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );
        //check that status updated correctly
        Event updatedEvent = eventRepo.findById(closedEvent.getEventId()).orElseThrow(() -> new EventNotFoundException(closedEvent.getEventId()));

        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(true, updatedEvent.getOpenStatus());
    }

    @Test
    public void openRegistration_EventNotFoundException_failure() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            //event with id -1 should not exist
            createURLWithPort("/events/-1/open"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void openRegistration_byUser_Return403() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + userToken);
        headers.add("Content-Type", "application/json");

        //we will attempt to open the closed event.
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/"+ closedEvent.getEventId() +"/open"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            Void.class
        );
        //check that status did not update.
        Event updatedEvent = eventRepo.findById(closedEvent.getEventId()).orElseThrow(() -> new EventNotFoundException(closedEvent.getEventId()));

        assertEquals(403, responseEntity.getStatusCode().value());
        assertEquals(false, updatedEvent.getOpenStatus());
    }

    @Test
    public void closeRegistration_byUser_Return403() throws Exception {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + userToken);
        headers.add("Content-Type", "application/json");

        //we will attempt to close the opened event.
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/"+ openEvent.getEventId() +"/close"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );
        //check that status did not update.
        Event updatedEvent = eventRepo.findById(openEvent.getEventId()).orElseThrow(() -> new EventNotFoundException(openEvent.getEventId()));

        assertEquals(403, responseEntity.getStatusCode().value());
        assertEquals(true, updatedEvent.getOpenStatus());
    }

    //eventRaffle tests
    @Test
    public void eventRaffle_Successful() {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/"+ closedEvent.getEventId() +"/raffle"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );

        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("Event #" + closedEvent.getEventId() + "'s raffle has been done", responseEntity.getBody());
    }

    @Test
    public void eventRaffle_EventNotFoundException_failure() {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            //event with id -1 should not exist
            createURLWithPort("/events/-1/raffle"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void eventRaffle_EventOpened_Return403() {
        // carry out request with jwt token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + adminToken);
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            createURLWithPort("/events/" + openEvent.getEventId() + "/raffle"),
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            String.class
        );

        assertEquals(403, responseEntity.getStatusCode().value());
        assertEquals("please close the event before raffling.", responseEntity.getBody());
    }


}
