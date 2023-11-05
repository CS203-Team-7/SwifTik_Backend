package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import com.swiftyticket.controllers.ZoneController;
import com.swiftyticket.dto.auth.AuthResponse;
import com.swiftyticket.dto.auth.SignInRequest;
import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.Ticket;
import com.swiftyticket.models.User;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.TicketRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.repositories.ZoneRepository;
import com.swiftyticket.services.AuthService;
import com.swiftyticket.services.EventService;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class ZoneIntegrationTest {
    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ZoneRepository zoneRepo;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuthService authService;

    private String adminToken;
    private String userToken;
    private Event openEvent;
    private Event closedEvent;
    private Date[] dates;
    private Date date;
 
    HttpHeaders headers = new HttpHeaders();
    TestRestTemplate testRestTemplate = new TestRestTemplate();


    @BeforeEach
    void setUp() throws Exception{
        log.info("starting setup");
        String password = "GoodPassword123!";
        String encodedPassword = new BCryptPasswordEncoder().encode(password);

        User newUser = new User("newUser@email.com", encodedPassword, new Date(), "+6582887066", Role.USER, true);
        userRepo.save(newUser);

        User newAdmin = new User("newAdmin@email.com", encodedPassword, new Date(), "+6887662344", Role.ADMIN, true);
        userRepo.save(newAdmin);

        AuthResponse adminLogin = authService.signIn(new SignInRequest("newAdmin@email.com", password));
        adminToken = adminLogin.getToken();

        AuthResponse userLogin = authService.signIn(new SignInRequest("newUser@email.com", password));
        userToken = userLogin.getToken();

        log.info(""+userToken);
        log.info(""+adminToken);

        //create events
        //create date array for the creation of event.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  
        date = sdf.parse("07/03/2023");
        dates = new Date[]{date};

        //create events.
        List<String> artists = new ArrayList<>();
        artists.add("Taylor Swift");

        openEvent = new Event("Swifty Concert", artists, Arrays.asList(dates), "Singapore Indoor Stadium", 10000);
        openEvent.setOpen4Registration(true);
        eventRepo.save(openEvent);

        closedEvent = new Event("Swifty Concert 2 Electric Boogalo", artists, Arrays.asList(dates), "Singapore Indoor Stadium", 10000);
        eventRepo.save(closedEvent);
    }

    @AfterEach
    void tearDown() {
        eventRepo.deleteAll();
        userRepo.deleteAll();
        zoneRepo.deleteAll();
    }

    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }

    @Test
    void addEvent_UserNotAuthorized_Return403() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + userToken);


        ZoneRequest zoneRequest = new ZoneRequest(10, "testZone", date, 12);

        ResponseEntity<Zones> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/events/" + openEvent.getEventId() + "/createZone"),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Zones.class
                );

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    void addZone_EventNotFound_ThrowException() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + adminToken);


        ZoneRequest zoneRequest = new ZoneRequest(10, "testZone", date, 12);

        HttpEntity<ZoneRequest> entity = new HttpEntity<>(zoneRequest, headers);

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
            //id with -1 will never exist
                createURLWithPort("/events/-1/createZone"),
                HttpMethod.POST,
                entity,
                Void.class
                );

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void addEvent_Successful() {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + adminToken);


        ZoneRequest zoneRequest = new ZoneRequest(10, "testZone", date, 12);

        HttpEntity<ZoneRequest> entity = new HttpEntity<>(zoneRequest, headers);

        ResponseEntity<Zones> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/events/" + openEvent.getEventId() + "/createZone"),
                HttpMethod.POST,
                entity,
                Zones.class
                );

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void getZones_Successful() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + userToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<Zones>> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/events/" + openEvent.getEventId() + "/zones"),
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Zones>>(){}
        );

        // Check the HTTP status code
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }
    
    @Test
    void getZones_EventNotFound_Return404() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + userToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            //negative id does not exist
            createURLWithPort("/events/-1/zones"),
            HttpMethod.GET, entity, String.class
        );

        // Check the HTTP status code
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Event ID " + (-1) + " could not be found.", responseEntity.getBody());
    }


    //preRegister tests
    //when the event is closed, is it supposed to return 403?
    // @Test
    // void preRegister_EventClosed_Return403() {
    //     Zones zone = new Zones(12, "test", date, 12, closedEvent);
    //     zoneRepo.save(zone);
        
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    //     headers.add("Content-Type", "application/json");
    //     headers.add("Authorization", "Bearer " + userToken);

    //     ResponseEntity<String> responseEntity = testRestTemplate.exchange(
    //             createURLWithPort("/events/" + closedEvent.getEventId() + "/zone=" + zone.getZoneId() + "/preRegister"),
    //             HttpMethod.PUT,
    //             new HttpEntity<>(headers),
    //             String.class
    //             );

    //     assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    //     assertEquals("The Pre-egistration has not yet opened, or Pre-registration has closed, join us next time!", responseEntity.getBody());
    // }


    // @Test
    // void preRegister_EventIDNotFound_Return404() {
    //     Zones zone = new Zones(12, "test", date, 12, openEvent);
    //     zoneRepo.save(zone);
        
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    //     headers.add("Content-Type", "application/json");
    //     headers.add("Authorization", "Bearer " + userToken);

    //     ResponseEntity<String> responseEntity = testRestTemplate.exchange(
    //         //negative id will never exist
    //             createURLWithPort("/events/-1/zone=" + zone.getZoneId() + "/preRegister"),
    //             HttpMethod.PUT,
    //             new HttpEntity<>(headers),
    //             String.class
    //             );

    //     assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    //     assertEquals("Event ID " + (-1) + " could not be found.", responseEntity.getBody());
    // }

    // @Test
    // void preRegister_ZoneIDNotFound_Return404() {
    //     Zones zone = new Zones(12, "test", date, 12, openEvent);
    //     zoneRepo.save(zone);
        
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    //     headers.add("Content-Type", "application/json");
    //     headers.add("Authorization", "Bearer " + userToken);

    //     ResponseEntity<String> responseEntity = testRestTemplate.exchange(
    //         //negative id for zone will never exist
    //             createURLWithPort("/events/" + openEvent.getEventId() + "/zone=-1/preRegister"),
    //             HttpMethod.PUT,
    //             new HttpEntity<>(headers),
    //             String.class
    //             );

    //     assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    //     assertNotNull(responseEntity.getBody());
    // }

    // @Test
    // void preRegister_Successful() {
    //     Zones zone = new Zones(12, "test", date, 12, openEvent);
    //     zoneRepo.save(zone);
        
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    //     headers.add("Content-Type", "application/json");
    //     headers.add("Authorization", "Bearer " + userToken);

    //     ResponseEntity<String> responseEntity = testRestTemplate.exchange(
    //             createURLWithPort("/events/" + openEvent.getEventId() + "/zone=" + zone.getZoneId() + "/preRegister"),
    //             HttpMethod.PUT,
    //             new HttpEntity<>(headers),
    //             String.class
    //             );

    //     assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    // }

}
