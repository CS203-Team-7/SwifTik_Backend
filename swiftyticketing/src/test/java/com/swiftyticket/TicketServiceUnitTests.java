package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swiftyticket.exceptions.*;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.Ticket;
import com.swiftyticket.models.User;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.TicketRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.repositories.ZoneRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.swiftyticket.services.implementations.JwtServiceImpl;
import com.swiftyticket.services.implementations.TicketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {TicketServiceImpl.class})
@ExtendWith(SpringExtension.class)
class TicketServiceUnitTests {
    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private JwtServiceImpl jwtServiceImpl;

    @MockBean
    private TicketRepository ticketRepository;

    @Autowired
    private TicketServiceImpl ticketServiceImpl;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ZoneRepository zoneRepository;

    // Helper method to create a zone object for testing
    private Zones getZone() {
        Zones zone = new Zones();
        zone.setEvent(new Event());
        zone.setPreRegisteredUsers4Zone(new ArrayList<>());
        zone.setTicketList(new ArrayList<>());
        zone.setTicket_price(10.0);
        zone.setTicketsLeft(1);
        zone.setUser_count(3);
        zone.setWinnerList(new ArrayList<>());
        zone.setZoneCapacity(1);
        zone.setZoneDate(new Date());
        zone.setZoneId(1);
        zone.setZoneName("Zone 1");
        return zone;
    }

    // listTickets() tests
    @Test
    void listTickets_Successful() {
        // Arrange
        ArrayList<Ticket> ticketList = new ArrayList<>();

        // Act
        when(ticketRepository.findAll()).thenReturn(ticketList);
        List<Ticket> actualListTicketsResult = ticketServiceImpl.listTickets();

        // Assert
        verify(ticketRepository).findAll();
        assertTrue(actualListTicketsResult.isEmpty());
        assertSame(ticketList, actualListTicketsResult);
    }

    @Test
    void listTickets_NoTickets_ThrowException() {
        // Arrange and Act
        when(ticketRepository.findAll()).thenThrow(new PurchaseException());

        // Assert
        assertThrows(PurchaseException.class, () -> ticketServiceImpl.listTickets());
        verify(ticketRepository).findAll();
    }

    // getTicket() tests
    @Test
    void getTicket_ValidTicket_Successful() {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setForUser(new User());
        ticket.setForZone(getZone());
        ticket.setTicketId(1);
        ticket.setUserEmail("test@gmail.com");
        ticket.setZonename("Zone 1");
        Optional<Ticket> ofResult = Optional.of(ticket);

        // Act
        when(ticketRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        Ticket actualTicket = ticketServiceImpl.getTicket(1);

        // Assert
        verify(ticketRepository).findById(Mockito.<Integer>any());
        assertSame(ticket, actualTicket);
    }

    @Test
    void getTicket_NoSuchTicket_ThrowException() {
        // Arrange
        Optional<Ticket> emptyResult = Optional.empty();

        // Act
        when(ticketRepository.findById(Mockito.<Integer>any())).thenReturn(emptyResult);

        // Assert
        assertThrows(TicketNotFoundException.class, () -> ticketServiceImpl.getTicket(1));
        verify(ticketRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void getTicket_InvalidInput_ThrowException() {
        // Arrange and Act
        when(ticketRepository.findById(Mockito.<Integer>any())).thenThrow(new PurchaseException());

        // Assert
        assertThrows(PurchaseException.class, () -> ticketServiceImpl.getTicket(1));
        verify(ticketRepository).findById(Mockito.<Integer>any());
    }

    // purchaseTicket() tests
    @Test
    void purchaseTicket_NotWinner_ThrowException() {

        // Arrange and Act
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn("test");

        User user = new User();
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("ILoveYou");
        user.setPhoneNumber("1234567890");
        user.setPreRegisteredEvents(new ArrayList<>());
        user.setPreRegisteredZones(new ArrayList<>());
        user.setRole(Role.USER);
        user.setTicketsBought(new ArrayList<>());
        user.setVerified(true);
        user.setZonesWon(new ArrayList<>());
        Optional<User> userResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(userResult);

        Event event = new Event();
        event.setArtists(new ArrayList<>());
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setPreRegisteredUsers4Event(new ArrayList<>());
        event.setRaffleRound(1);
        event.setUser_count(3);
        event.setVenue("Venue 1");
        event.setVenueCapacity(1);
        event.setZoneList(new ArrayList<>());
        Optional<Event> eventResult1 = Optional.of(event);
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(eventResult1);

        Event event2 = new Event();
        event2.setArtists(new ArrayList<>());
        event2.setDates(new ArrayList<>());
        event2.setEventId(1);
        event2.setEventName("Event 1");
        event2.setOpen4Registration(true);
        event2.setPreRegisteredUsers4Event(new ArrayList<>());
        event2.setRaffleRound(1);
        event2.setUser_count(3);
        event2.setVenue("Venue 1");
        event2.setVenueCapacity(1);
        event2.setZoneList(new ArrayList<>());

        Zones zones = new Zones();
        zones.setEvent(event2);
        zones.setPreRegisteredUsers4Zone(new ArrayList<>());
        zones.setTicketList(new ArrayList<>());
        zones.setTicket_price(10.0);
        zones.setTicketsLeft(1);
        zones.setUser_count(3);
        zones.setWinnerList(new ArrayList<>());
        zones.setZoneCapacity(1);
        zones.setZoneDate(new Date());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        Optional<Zones> zoneResult = Optional.of(zones);
        when(zoneRepository.findByZoneIdAndEvent(Mockito.<Integer>any(), Mockito.<Event>any())).thenReturn(zoneResult);

        // Assert
        assertThrows(PurchaseException.class, () -> ticketServiceImpl.purchaseTicket("Bearer Token", 1, 1));
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(zoneRepository).findByZoneIdAndEvent(Mockito.<Integer>any(), Mockito.<Event>any());
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void purchaseTicket_InvalidZone_ThrowException() {
        // Arrange and Act
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn("test");
        User user = new User();
        user.setDateOfBirth(new Date());
        user.setEmail("test@gmail.com");
        user.setPassword("ILoveYou");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setTicketsBought(new ArrayList<>());
        user.setUserId(1);
        user.setVerified(true);
        user.setZonesWon(new ArrayList<>());
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        Event event = new Event();
        event.setArtists(new ArrayList<>());
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setPreRegisteredUsers4Event(new ArrayList<>());
        event.setRaffleRound(1);
        event.setUser_count(3);
        event.setVenue("Venue 1");
        event.setVenueCapacity(1);
        event.setZoneList(new ArrayList<>());
        Optional<Event> ofResult2 = Optional.of(event);
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult2);
        when(zoneRepository.findByZoneIdAndEvent(Mockito.<Integer>any(), Mockito.<Event>any()))
                .thenThrow(new ZoneNotFoundException("Invalid zone for " + event.getEventName()));

        // Assert
        assertThrows(ZoneNotFoundException.class, () -> ticketServiceImpl.purchaseTicket("Bearer Token", 1, 1));
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(zoneRepository).findByZoneIdAndEvent(Mockito.<Integer>any(), Mockito.<Event>any());
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    /**
     * Method under test: {@link TicketServiceImpl#purchaseTicket(String, Integer, Integer)}
     */
    @Test
    void purchaseTicket_InvalidUser_ThrowException() {
        // Arrange and Act
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn("test");
        Optional<User> emptyResult = Optional.empty();
        when(userRepository.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);

        Event event = new Event();
        event.setArtists(new ArrayList<>());
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setPreRegisteredUsers4Event(new ArrayList<>());
        event.setRaffleRound(1);
        event.setUser_count(3);
        event.setVenue("Venue 1");
        event.setVenueCapacity(1);
        event.setZoneList(new ArrayList<>());
        Optional<Event> ofResult = Optional.of(event);
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);

        // Assert
        assertThrows(UserNotFoundException.class, () -> ticketServiceImpl.purchaseTicket("Bearer Token", 1, 1));
        verify(userRepository).findByEmail(Mockito.<String>any());
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void purchaseTicket_InvalidEvent_ThrowException() {
        // Arrange and Act
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn("test");
        Optional<Event> emptyResult = Optional.empty();
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(emptyResult);

        // Assert
        assertThrows(EventNotFoundException.class, () -> ticketServiceImpl.purchaseTicket("Bearer Token", 1, 1));
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    // deleteTicket() tests
    @Test
    void deleteTicket_Successful() {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setForUser(new User());
        ticket.setForZone(getZone());
        ticket.setTicketId(1);
        ticket.setUserEmail("test@gmail.com");
        ticket.setZonename("Zone 1");
        Optional<Ticket> ofResult = Optional.of(ticket);

        // Act
        doNothing().when(ticketRepository).deleteById(Mockito.<Integer>any());
        when(ticketRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        ticketServiceImpl.deleteTicket(1);

        // Assert
        verify(ticketRepository).deleteById(Mockito.<Integer>any());
        verify(ticketRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void deleteTicket_InvalidTicket_ThrowException() {
        // Arrange and Act
        when(ticketRepository.findById(Mockito.<Integer>any())).thenReturn(null);

        // Assert
        assertThrows(TicketNotFoundException.class, () -> ticketServiceImpl.deleteTicket(1));
        verify(ticketRepository).findById(Mockito.<Integer>any());
    }
}

