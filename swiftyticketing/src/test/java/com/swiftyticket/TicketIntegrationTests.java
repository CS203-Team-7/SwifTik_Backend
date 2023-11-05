package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
import com.swiftyticket.services.EventService;
import com.swiftyticket.services.TicketService;
import com.swiftyticket.services.ZoneService;
import com.swiftyticket.services.implementations.AuthServiceImpl;
import com.swiftyticket.services.implementations.SmsServiceImpl;
import com.swiftyticket.services.implementations.TicketServiceImpl;

import jakarta.transaction.Transactional;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j

public class TicketIntegrationTests {
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
    private ZoneRepository zoneRepo;

    @Autowired
    private TicketRepository ticketRepo;
    
    @Autowired
    private AuthServiceImpl authServ;

    @Autowired
    private EventService eventServ;

    @Autowired 
    private ZoneService zoneServ;

    @Autowired
    private TicketServiceImpl tickServ;

    private String adminToken;
    private String userToken;
    private Event event;
    private Zones zone;


    @BeforeEach
    void setUp() throws Exception{
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

        event = eventServ.addEvent(new Event("concert", Arrays.asList(artists), Arrays.asList(dates), "some stage", 50));
        //eventRepo.save(event);

        //create a zone for the event
        //addZone(ZoneRequest zoneReq, Event event)
        //set capacity to one so we can gurantee a winner
        zone = zoneServ.addZone(new ZoneRequest(1, "testZone", date, 450),event);
       
        //open the event to allow users to register, then register the to-be winner inside.
        eventServ.openEvent(event.getEventId());
        zoneServ.joinRaffle("Bearer " + userToken, event.getEventId(), zone.getZoneId());
    
        //close the event, raffle to produce a winner.
    
        eventServ.closeEvent(event.getEventId());
        eventServ.raffle(event.getEventId());
        
        //now newUser should be a winner, and newAdmin a loser
        }
        

    @AfterEach
    void tearDown(){
        ticketRepo.deleteAll();
        eventRepo.deleteAll();
        zoneRepo.deleteAll();
        userRepo.deleteAll();
        

    }

    private String createURLWithPort(String uri)
    {
        return baseUrl + port + uri;
    }

    @Test
    public void ticketPurchase_Valid_ReturnTicket() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + userToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Ticket> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/tickets/purchase/eventId=" + event.getEventId() + ",zoneId=" + zone.getZoneId()),
                HttpMethod.POST, entity, Ticket.class
        );
            
        assertEquals(201, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void ticketPurchase_Invalid_Return403() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        //admin isnt a winner as he didn't participate. should fail. 
        headers.add("Authorization", "Bearer " + adminToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/tickets/purchase/eventId=" + event.getEventId() + ",zoneId=" + zone.getZoneId()),
                HttpMethod.POST, entity, String.class
        );
            
        assertEquals(403, responseEntity.getStatusCode().value());
        assertEquals("Either you have not won a raffle for this zone, or you have already bought a ticket.",responseEntity.getBody());
    }

    @Test
    public void getTicket_Valid_ReturnTicket() throws Exception {
        //generate a ticket by making the winner buy a ticket.
        Ticket ticket = tickServ.purchaseTicket("Bearer " + userToken, event.getEventId(), zone.getZoneId());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + userToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Ticket> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/tickets/" + ticket.getTicketId()),
                HttpMethod.GET, entity, Ticket.class
        );
            
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
    }


    @Test
    public void getTicket_Invalid_Return404() throws Exception {
        //generate a ticket by making the winner buy a ticket.
        Ticket ticket = tickServ.purchaseTicket("Bearer " + userToken, event.getEventId(), zone.getZoneId());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + adminToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(
            //ticket with id -1 shouldnt exist here
                createURLWithPort("/tickets/-1"),
                HttpMethod.GET, entity, String.class
        );
            
        assertEquals(404, responseEntity.getStatusCode().value());
        assertEquals("Could not find ticket -1",responseEntity.getBody());
    }

    @Test
    public void getAllTickets_Valid_ReturnTickets() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + adminToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                createURLWithPort("/tickets"),
                HttpMethod.GET, entity, Void.class
        );
            
        assertEquals(200, responseEntity.getStatusCode().value());
    }
    
}
