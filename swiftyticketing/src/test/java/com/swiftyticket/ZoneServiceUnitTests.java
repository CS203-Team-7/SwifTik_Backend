package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneOffset;
import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.exceptions.WrongZoneDateException;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.repositories.ZoneRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.swiftyticket.services.implementations.JwtServiceImpl;
import com.swiftyticket.services.implementations.SmsServiceImpl;
import com.swiftyticket.services.implementations.ZoneServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import com.swiftyticket.exceptions.AlreadyPreRegisteredException;
import com.swiftyticket.exceptions.EventClosedException;
import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.exceptions.UserNotFoundException;

import com.swiftyticket.exceptions.ZoneNotFoundException;

import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;

@ContextConfiguration(classes = {ZoneServiceImpl.class})
@ExtendWith(SpringExtension.class)
class ZoneServiceUnitTests {
    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private JwtServiceImpl jwtServiceImpl;

    @MockBean
    private SmsServiceImpl smsServiceImpl;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ZoneRepository zoneRepository;

    @Autowired
    private ZoneServiceImpl zoneServiceImpl;

    @Test
    void addZone_NullDate_ThrowException() {
        // Arrange and Act
        ZoneRequest zoneReq = new ZoneRequest();

        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setZoneList(new ArrayList<>());

        // Assert
        assertThrows(WrongZoneDateException.class, () -> zoneServiceImpl.addZone(zoneReq, event));
    }

    @Test
    void addZone_CorrectDate_Successful() {
        // Arrange
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setZoneList(new ArrayList<>());

        Zones zones = new Zones();
        zones.setEvent(event);
        zones.setPreRegisteredUsers4Zone(new ArrayList<>());
        zones.setZoneCapacity(1);
        zones.setZoneDate(new Date());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        when(zoneRepository.save(Mockito.any())).thenReturn(zones);

        Event event2 = new Event();
        event2.setDates(new ArrayList<>());
        event2.setEventId(1);
        event2.setEventName("Event 1");
        event2.setOpen4Registration(true);
        event2.setZoneList(new ArrayList<>());
        when(eventRepository.save(Mockito.any())).thenReturn(event2);

        // Act
        ZoneRequest zoneReq = mock(ZoneRequest.class);
        when(zoneReq.getZoneDate())
                .thenReturn(new Date());

        ArrayList<Date> dateList = new ArrayList<>();
        dateList.add(new Date());
        Event event3 = mock(Event.class);
        when(event3.getZoneList()).thenReturn(new ArrayList<>());
        when(event3.getDates()).thenReturn(dateList);
        doNothing().when(event3).setDates(Mockito.any());
        doNothing().when(event3).setEventId(Mockito.<Integer>any());
        doNothing().when(event3).setEventName(Mockito.any());
        doNothing().when(event3).setOpen4Registration(anyBoolean());
        doNothing().when(event3).setZoneList(Mockito.any());
        event3.setDates(new ArrayList<>());
        event3.setEventId(1);
        event3.setEventName("Event 1");
        event3.setOpen4Registration(true);
        event3.setZoneList(new ArrayList<>());
        Zones actualAddZoneResult = zoneServiceImpl.addZone(zoneReq, event3);

        // Assert
        verify(zoneReq, atLeast(1)).getZoneDate();
        verify(zoneReq).getZoneName();
        verify(event3).getDates();
        verify(event3).getZoneList();
        verify(event3).setDates(Mockito.any());
        verify(event3).setEventId(Mockito.<Integer>any());
        verify(event3).setEventName(Mockito.any());
        verify(event3).setOpen4Registration(anyBoolean());
        verify(event3).setZoneList(Mockito.any());
        verify(eventRepository).save(Mockito.any());
        verify(zoneRepository).save(Mockito.any());
        assertSame(zones, actualAddZoneResult);
    }

    @Test
    void addZone_InvalidDate_ThrowException() {
        // Arrange
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setZoneList(new ArrayList<>());

        Zones zones = new Zones();
        zones.setEvent(event);
        zones.setZoneCapacity(1);
        zones.setZoneDate(new Date());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        when(zoneRepository.save(Mockito.any())).thenReturn(zones);

        Event event2 = new Event();
        event2.setDates(new ArrayList<>());
        event2.setEventId(1);
        event2.setEventName("Event 1");
        event2.setOpen4Registration(true);
        event2.setZoneList(new ArrayList<>());
        when(eventRepository.save(Mockito.any())).thenReturn(event2);

        // Act
        ZoneRequest zoneReq = mock(ZoneRequest.class);
        when(zoneReq.getZoneName()).thenReturn("Zone 1");
        when(zoneReq.getZoneDate())
                .thenReturn(new Date());

        ArrayList<Date> dateList = new ArrayList<>();
        dateList.add(new Date());
        Event event3 = mock(Event.class);
        Event event4 = new Event();
        when(event3.getZoneList()).thenThrow(new WrongZoneDateException(event4,
                new Date()));
        when(event3.getDates()).thenReturn(dateList);
        doNothing().when(event3).setDates(Mockito.any());
        doNothing().when(event3).setEventId(Mockito.<Integer>any());
        doNothing().when(event3).setEventName(Mockito.any());
        doNothing().when(event3).setOpen4Registration(anyBoolean());
        doNothing().when(event3).setZoneList(Mockito.any());
        event3.setArtists(new ArrayList<>());
        event3.setDates(new ArrayList<>());
        event3.setEventId(1);
        event3.setEventName("Event 1");
        event3.setOpen4Registration(true);
        event3.setZoneList(new ArrayList<>());

        // Assert
        assertThrows(WrongZoneDateException.class, () -> zoneServiceImpl.addZone(zoneReq, event3));
        verify(zoneReq, atLeast(1)).getZoneDate();
    }

    @Test
    void listZones_Successful() {
        // Arrange
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        ArrayList<Zones> zoneList = new ArrayList<>();
        event.setZoneList(zoneList);

        // Act and Assert
        List<Zones> actualListZonesResult = zoneServiceImpl.listZones(event);
        assertTrue(actualListZonesResult.isEmpty());
        assertSame(zoneList, actualListZonesResult);
    }

    /**
     * Method under test: {@link ZoneServiceImpl#listZones(Event)}
     */
    @Test
    void listZones_EmptyList_Successful() {
        // Arrange
        Event event = mock(Event.class);

        // Act
        ArrayList<Zones> zonesList = new ArrayList<>();
        when(event.getZoneList()).thenReturn(zonesList);
        doNothing().when(event).setDates(Mockito.any());
        doNothing().when(event).setEventId(Mockito.<Integer>any());
        doNothing().when(event).setEventName(Mockito.any());
        doNothing().when(event).setOpen4Registration(anyBoolean());
        doNothing().when(event).setZoneList(Mockito.any());
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setZoneList(new ArrayList<>());


        List<Zones> actualListZonesResult = zoneServiceImpl.listZones(event);

        // Assert
        verify(event).getZoneList();
        verify(event).setDates(Mockito.any());
        verify(event).setEventId(Mockito.<Integer>any());
        verify(event).setEventName(Mockito.any());
        verify(event).setOpen4Registration(anyBoolean());
        verify(event).setZoneList(Mockito.any());
        assertTrue(actualListZonesResult.isEmpty());
        assertSame(zonesList, actualListZonesResult);
    }

    // joinRaffle() tests
    @Test
    void joinRaffle_EventNotFound_ThrowException(){
        //Arrange
        Event event = mock(Event.class);
        when(event.getEventId()).thenReturn(1);

        Zones zones = mock(Zones.class);
        when(zones.getZoneId()).thenReturn(1);

        //Act
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn("username1");

        //Assert
        assertThrows(EventNotFoundException.class, () -> zoneServiceImpl.joinRaffle("sjfnajdnad", event.getEventId(), zones.getZoneId()));

        //to confirm that eventId is not found in eventRepository
        Optional<Event> emptyValue = Optional.empty();
        assertEquals(emptyValue, eventRepository.findById(event.getEventId()));

        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
    }

    @Test
    void joinRaffle_UserEmailNotFound_ThrowException() {
        //Arrange
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        Integer eventId = event.getEventId();

        Zones zones = new Zones();
        zones.setEvent(event);
        zones.setTicketList(new ArrayList<>());
        zones.setZoneCapacity(1);
        zones.setZoneDate(new Date());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        Integer zoneId = zones.getZoneId();

        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        String username = user.getUsername();
        Integer userId = user.getUserId();

        //Act
        Optional<Event> ofResult = Optional.of(event);
        when(eventRepository.findById(eventId)).thenReturn(ofResult);
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn(username);

        //Assert
        assertThrows(UserNotFoundException.class, () -> zoneServiceImpl.joinRaffle("sjfnajdnad", eventId, zoneId));

        //to confirm that eventId is not found in eventRepository
        Optional<User> emptyValue = Optional.empty();
        assertEquals(emptyValue, userRepository.findById(userId));

        verify(eventRepository).findById(eventId);
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
    }

    @Test
    void joinRaffle_ZoneNotFound_ThrowException() {
        //Arrange
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        //if this is uncommented, it won't work for some reason
        //EventRepository eventRepository = mock(EventRepository.class);
        Integer eventId = event.getEventId();

        Zones zones = new Zones();
        zones.setEvent(event);
        zones.setTicketList(new ArrayList<>());
        zones.setZoneCapacity(1);
        zones.setZoneDate(new Date());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        Integer zoneId = zones.getZoneId();

        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        String username = user.getUsername();
        String email = user.getEmail();

        //Act
        Optional<Event> ofEventResult = Optional.of(event);
        when(eventRepository.findById(eventId)).thenReturn(ofEventResult);
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn(username);

        Optional<User> ofUserResult = Optional.of(user);
        when(userRepository.findByEmail(email)).thenReturn(ofUserResult);

        //Assert
        assertThrows(ZoneNotFoundException.class, () -> zoneServiceImpl.joinRaffle("sjfnajdnad", eventId, zoneId));

        verify(eventRepository).findById(eventId);
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
        verify(userRepository).findByEmail(email);
    }


    @Test
    void joinRaffle_EventIsClosed_ThrowException() {
         //Arrange
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");     
        //set to false to simulate a closed event
        event.setOpen4Registration(false);
        Integer eventId = event.getEventId();

        Zones zones = new Zones();
        zones.setEvent(event);
        zones.setTicketList(new ArrayList<>());
        zones.setZoneCapacity(1);
        zones.setZoneDate(new Date());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        Integer zoneId = zones.getZoneId();

        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        String username = user.getUsername();
        String email = user.getEmail();

        //Act
        Optional<Event> ofEventResult = Optional.of(event);
        when(eventRepository.findById(eventId)).thenReturn(ofEventResult);
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn(username);

        Optional<User> ofUserResult = Optional.of(user);
        when(userRepository.findByEmail(email)).thenReturn(ofUserResult);

        Optional<Zones> ofZonesResult = Optional.of(zones);
        when(zoneRepository.findByZoneIdAndEvent(zoneId, event)).thenReturn(ofZonesResult);

        //Assert
        assertThrows(EventClosedException.class, () -> zoneServiceImpl.joinRaffle("sjfnajdnad", eventId, zoneId));

        verify(eventRepository).findById(eventId);
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
        verify(userRepository).findByEmail(email);
        verify(zoneRepository).findByZoneIdAndEvent(zoneId, event);
    }


    @Test
    void joinRaffle_UserAlreadyPreRegistered_ThrowException() {
        //Arrange
        Event event = new Event();
        event.setEventId(1);
        event.setEventName("Event 1");     
        event.setOpen4Registration(true);
        Integer eventId = event.getEventId();
        List<User> preRegisteredList = new ArrayList<>();
        event.setPreRegisteredUsers4Event(preRegisteredList);

        Zones zones = new Zones();
        zones.setEvent(event);
        zones.setTicketList(new ArrayList<>());
        zones.setZoneCapacity(1);
        zones.setZoneDate(new Date());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        Integer zoneId = zones.getZoneId();

        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        String username = user.getUsername();
        String email = user.getEmail();

        preRegisteredList.add(user);

        //Act
        Optional<Event> ofEventResult = Optional.of(event);
        when(eventRepository.findById(eventId)).thenReturn(ofEventResult);
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn(username);

        Optional<User> ofUserResult = Optional.of(user);
        when(userRepository.findByEmail(email)).thenReturn(ofUserResult);

        Optional<Zones> ofZonesResult = Optional.of(zones);
        when(zoneRepository.findByZoneIdAndEvent(zoneId, event)).thenReturn(ofZonesResult);

        //Assert
        assertThrows(AlreadyPreRegisteredException.class, () -> zoneServiceImpl.joinRaffle("sjfnajdnad", eventId, zoneId));

        verify(eventRepository).findById(eventId);
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
        verify(userRepository).findByEmail(email);
        verify(zoneRepository).findByZoneIdAndEvent(zoneId, event);
    }


    @Test
    void joinRaffle_Successful() {
        Event event = new Event();
        event.setEventId(1);
        event.setEventName("Event 1");     
        event.setOpen4Registration(true);
        event.setPreRegisteredUsers4Event(new ArrayList<>());
        Integer eventId = event.getEventId();

        Zones zones = new Zones();
        zones.setEvent(event);
        zones.setTicketList(new ArrayList<>());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        zones.setPreRegisteredUsers4Zone(new ArrayList<>());
        Integer zoneId = zones.getZoneId();

        User user = new User();
        user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        user.setEmail("test@gmail.com");
        user.setPassword("Iloveyou1!");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.USER);
        user.setVerified(true);
        user.setPreRegisteredZones(new ArrayList<>());
        user.setPreRegisteredEvents(new ArrayList<>());
        String username = user.getUsername();
        String email = user.getEmail();


        Optional<Event> ofEventResult = Optional.of(event);
        when(eventRepository.findById(eventId)).thenReturn(ofEventResult);
        when(jwtServiceImpl.extractUserName(Mockito.<String>any())).thenReturn(username);

        Optional<User> ofUserResult = Optional.of(user);
        when(userRepository.findByEmail(email)).thenReturn(ofUserResult);

        Optional<Zones> ofZonesResult = Optional.of(zones);
        when(zoneRepository.findByZoneIdAndEvent(zoneId, event)).thenReturn(ofZonesResult);

        when(eventRepository.save(event)).thenReturn(event);
        when(userRepository.save(user)).thenReturn(user);
        when(zoneRepository.save(zones)).thenReturn(zones);

        String result = "Successfully joined the raffle for: " + zones.getZoneName() + " on " + zones.getZoneDate() + " for " + event.getEventName();


        //Assert
        assertEquals(result, zoneServiceImpl.joinRaffle("sjfnajdnad", eventId, zoneId));

        verify(eventRepository).findById(eventId);
        verify(jwtServiceImpl).extractUserName(Mockito.<String>any());
        verify(userRepository).findByEmail(email);
        verify(zoneRepository).findByZoneIdAndEvent(zoneId, event);

        verify(eventRepository).save(event);
        verify(userRepository).save(user);
        verify(zoneRepository).save(zones);
    }


    //raffle() tests

    //helper method
    public void setUserDetails(User user) {
        user.setZonesWon(new ArrayList<Zones>());
        user.setPreRegisteredEvents(new ArrayList<Event>());
        user.setPreRegisteredZones(new ArrayList<Zones>());
    }

    @Test
    void raffleFirstRound_MoreTicketsThanUsers_Successful() {
        //set event
        Event event = new Event();
        event.setEventId(1);
        event.setRaffleRound(0);

        //set user
        User user1 = new User();
        setUserDetails(user1);

        User user2 = new User();
        setUserDetails(user2);

        User user3 = new User();
        setUserDetails(user3);

        //set zone
        Zones zone = new Zones();
        zone.setWinnerList(new ArrayList<>());
        zone.setEvent(event);
        zone.setTicketsLeft(4);

        //put a list of pre-registered users for event and zone
        ArrayList<User> userList4Zone = new ArrayList<>();
        userList4Zone.add(user1);
        userList4Zone.add(user2);
        userList4Zone.add(user3);

        ArrayList<User> userList4Event = userList4Zone;

        zone.setPreRegisteredUsers4Zone(userList4Zone);
        event.setPreRegisteredUsers4Event(userList4Event);

        ArrayList<User> expectedWinnersList = new ArrayList<>(Arrays.asList(user1, user2, user3));

        //Act
        zoneServiceImpl.raffle(zone);

        //Assert
        //put in HashSet because the arrangement of getWinnerList is randomized everytime the program runs.
        assertEquals(new HashSet<User>(expectedWinnersList), new HashSet<User>(zone.getWinnerList()));
        assertEquals(new ArrayList<User>(), zone.getPreRegisteredUsers4Zone());

        //for users
        assertEquals(new ArrayList<Event>(), user1.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user1.getPreRegisteredZones());

        assertEquals(new ArrayList<Event>(), user2.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user2.getPreRegisteredZones());

        assertEquals(new ArrayList<Event>(), user3.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user3.getPreRegisteredZones());

        assertEquals(new ArrayList<Zones>(Arrays.asList(zone)), user1.getZonesWon());
        //assertEquals(expectedWinnersList, zone.getWinnerList());
        
    }

    @Test
    void raffleFirstRound_LessTicketsThanUsers_Successful() {
        //set event
        Event event = new Event();
        event.setEventId(1);
        event.setRaffleRound(0);

        //set user
        User user1 = new User();
        setUserDetails(user1);

        User user2 = new User();
        setUserDetails(user2);

        User user3 = new User();
        setUserDetails(user3);

        //set zone
        Zones zone = new Zones();
        zone.setWinnerList(new ArrayList<>());
        zone.setEvent(event);
        zone.setTicketsLeft(2);

        //put a list of pre-registered users for event and zone
        ArrayList<User> userList4Zone = new ArrayList<>();
        userList4Zone.add(user1);
        userList4Zone.add(user2);
        userList4Zone.add(user3);

        ArrayList<User> userList4Event = userList4Zone;

        zone.setPreRegisteredUsers4Zone(userList4Zone);
        event.setPreRegisteredUsers4Event(userList4Event);


        //Act
        zoneServiceImpl.raffle(zone);

        //Assert
        //check the size instead of the actual array since we don't know who is going to win
        assertEquals(2, zone.getWinnerList().size());
        assertEquals(1, zone.getPreRegisteredUsers4Zone().size());

        //for users
        assertEquals(new ArrayList<Event>(), user1.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user1.getPreRegisteredZones());

        assertEquals(new ArrayList<Event>(), user2.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user2.getPreRegisteredZones());

        assertEquals(new ArrayList<Event>(), user3.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user3.getPreRegisteredZones());

        //assertEquals(expectedWinnersList, zone.getWinnerList());

        
    }

    @Test
    void raffleSecondRound_MoreTicketsThanUsers_Successful() {
        //set event
        Event event = new Event();
        event.setEventId(1);
        event.setRaffleRound(1);

        //set user
        User user1 = new User();
        setUserDetails(user1);
        // user1.setZonesWon(new ArrayList<Zones>());
        // user1.setPreRegisteredEvents(new ArrayList<Event>());
        // user1.setPreRegisteredZones(new ArrayList<Zones>());

        User user2 = new User();
        setUserDetails(user2);

        User user3 = new User();
        setUserDetails(user3);

        //user who won in the first raffle
        User userPreviouslyWon = new User();
        setUserDetails(userPreviouslyWon);

        //set zone
        Zones zone = new Zones();
        zone.setWinnerList(new ArrayList<>());
        zone.getWinnerList().add(userPreviouslyWon);

        zone.setEvent(event);
        zone.setTicketsLeft(4);

        zone.getWinnerList().add(userPreviouslyWon);

        //put a list of pre-registered users for event and zone
        ArrayList<User> userList4Zone = new ArrayList<>();
        userList4Zone.add(user1);
        userList4Zone.add(user2);
        userList4Zone.add(user3);

        ArrayList<User> userList4Event = userList4Zone;

        zone.setPreRegisteredUsers4Zone(userList4Zone);
        event.setPreRegisteredUsers4Event(userList4Event);

        
        ArrayList<User> expectedWinnersList = new ArrayList<>(Arrays.asList(user1, user2, user3));


        //Act
        zoneServiceImpl.raffle(zone);

        //Assert
        //put in HashSet because the arrangement of getWinnerList is randomized everytime the program runs.
        assertEquals(new HashSet<User>(expectedWinnersList), new HashSet<User>(zone.getWinnerList()));
        assertEquals(0, zone.getPreRegisteredUsers4Zone().size());

        //for users
        assertEquals(new ArrayList<Event>(), user1.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user1.getPreRegisteredZones());

        assertEquals(new ArrayList<Event>(), user2.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user2.getPreRegisteredZones());

        assertEquals(new ArrayList<Event>(), user3.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user3.getPreRegisteredZones());


        assertEquals(new ArrayList<Zones>(Arrays.asList(zone)), user1.getZonesWon());
        //assertEquals(expectedWinnersList, zone.getWinnerList());

        //check if zonesWon has been removed, as the previously won user should not be able to buy the ticket for the current raffle
        //if they have won in the previous raffle
        assertEquals(new ArrayList<Zones>(), userPreviouslyWon.getZonesWon());
        
    }

    @Test
    void raffleSecondRound_LessTicketsThanUsers_Successful() {
        //set event
        Event event = new Event();
        event.setEventId(1);
        event.setRaffleRound(1);

        //set user
        User user1 = new User();
        setUserDetails(user1);
        // user1.setZonesWon(new ArrayList<Zones>());
        // user1.setPreRegisteredEvents(new ArrayList<Event>());
        // user1.setPreRegisteredZones(new ArrayList<Zones>());

        User user2 = new User();
        setUserDetails(user2);

        User user3 = new User();
        setUserDetails(user3);

        //user who won in the first raffle
        User userPreviouslyWon = new User();
        setUserDetails(userPreviouslyWon);

        //set zone
        Zones zone = new Zones();
        zone.setWinnerList(new ArrayList<>());
        zone.getWinnerList().add(userPreviouslyWon);

        zone.setEvent(event);
        zone.setTicketsLeft(2);

        zone.getWinnerList().add(userPreviouslyWon);

        //put a list of pre-registered users for event and zone
        ArrayList<User> userList4Zone = new ArrayList<>();
        userList4Zone.add(user1);
        userList4Zone.add(user2);
        userList4Zone.add(user3);

        ArrayList<User> userList4Event = userList4Zone;

        zone.setPreRegisteredUsers4Zone(userList4Zone);
        event.setPreRegisteredUsers4Event(userList4Event);


        //Act
        zoneServiceImpl.raffle(zone);

        //Assert
        //check the size instead of the actual array since we don't know who is going to win
        assertEquals(2, zone.getWinnerList().size());
        assertEquals(1, zone.getPreRegisteredUsers4Zone().size());


        //for users
        assertEquals(new ArrayList<Event>(), user1.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user1.getPreRegisteredZones());

        assertEquals(new ArrayList<Event>(), user2.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user2.getPreRegisteredZones());

        assertEquals(new ArrayList<Event>(), user3.getPreRegisteredEvents());
        assertEquals(new ArrayList<Zones>(), user3.getPreRegisteredZones());

        //check if zonesWon has been removed, as the previously won user should not be able to buy the ticket for the current raffle
        //if they have won in the previous raffle
        assertEquals(new ArrayList<Zones>(), userPreviouslyWon.getZonesWon());
        
    }


}

