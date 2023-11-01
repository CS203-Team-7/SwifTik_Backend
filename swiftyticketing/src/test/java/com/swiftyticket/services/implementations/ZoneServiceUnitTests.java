package com.swiftyticket.services.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.exceptions.WrongZoneDateException;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Role;
import com.swiftyticket.models.User;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.repositories.ZoneRepository;

@ContextConfiguration(classes = {ZoneServiceImpl.class})
@ExtendWith(SpringExtension.class)
class ZoneServiceUnitTests {
    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private JwtServiceImpl jwtServiceImpl;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ZoneRepository zoneRepository;

    @Autowired
    private ZoneServiceImpl zoneServiceImpl;

    // addZone() tests
    @Test
    void addZone_WrongEvent_ThrowException() {
        // Arrange
        ZoneRequest zoneReq = new ZoneRequest();
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setZoneList(new ArrayList<>());

        // Act and Assert
        assertThrows(WrongZoneDateException.class, () -> zoneServiceImpl.addZone(zoneReq, event));
    }

    @Test
    void addZone_ValidDate_Successful() {
        // Arrange
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setZoneList(new ArrayList<>());

        Zones zone = new Zones();
        zone.setEvent(event);
        zone.setZoneCapacity(1);
        zone.setZoneDate(new Date());
        zone.setZoneId(1);
        zone.setZoneName("Zone 1");
        when(zoneRepository.save(Mockito.<Zones>any())).thenReturn(zone);

        // Act
        Event event2 = new Event();
        event2.setDates(new ArrayList<>());
        event2.setEventId(1);
        event2.setEventName("Event 1");
        event2.setOpen4Registration(true);
        event2.setZoneList(new ArrayList<>());
        when(eventRepository.save(Mockito.<Event>any())).thenReturn(event2);

        ZoneRequest zoneReq = mock(ZoneRequest.class);
        when(zoneReq.getTicketPrice()).thenReturn(10.0d);
        when(zoneReq.getZoneCapacity()).thenReturn(1);
        when(zoneReq.getZoneName()).thenReturn("Zone 1");
        when(zoneReq.getZoneDate())
                .thenReturn(new Date());

        ArrayList<Date> dateList = new ArrayList<>();
        dateList.add(new Date());
        Event event3 = mock(Event.class);
        when(event3.getZoneList()).thenReturn(new ArrayList<>());
        when(event3.getDates()).thenReturn(dateList);
        Zones actualAddZoneResult = zoneServiceImpl.addZone(zoneReq, event3);

        // Assert
        assertSame(zone, actualAddZoneResult);
    }


    @Test
    void addZone_WrongDate_ThrowException() {
        // Arrange
        Event event = new Event();
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setZoneList(new ArrayList<>());

        Zones zones = new Zones();
        zones.setEvent(event);
        zones.setTicketList(new ArrayList<>());
        zones.setZoneCapacity(1);
        zones.setZoneDate(new Date());
        zones.setZoneId(1);
        zones.setZoneName("Zone 1");
        when(zoneRepository.save(Mockito.<Zones>any())).thenReturn(zones);

        Event event2 = new Event();
        event2.setDates(new ArrayList<>());
        event2.setEventId(1);
        event2.setEventName("Event 1");
        event2.setOpen4Registration(true);
        event2.setZoneList(new ArrayList<>());
        when(eventRepository.save(Mockito.<Event>any())).thenReturn(event2);

        // Act
        ZoneRequest zoneReq = mock(ZoneRequest.class);
        when(zoneReq.getZoneCapacity()).thenReturn(1);
        when(zoneReq.getZoneName()).thenReturn("Zone 1");
        when(zoneReq.getZoneDate()).thenReturn(new Date());

        ArrayList<Date> dateList = new ArrayList<>();
        dateList.add(new Date());
        Event event3 = mock(Event.class);
        Event event4 = new Event();
        when(event3.getZoneList()).thenThrow(new WrongZoneDateException(event4, new Date()));
        when(event3.getDates()).thenReturn(dateList);
        doNothing().when(event3).setDates(Mockito.<List<Date>>any());
        doNothing().when(event3).setEventId(Mockito.<Integer>any());
        doNothing().when(event3).setZoneList(Mockito.<List<Zones>>any());
        event3.setDates(new ArrayList<>());
        event3.setEventId(1);
        event3.setZoneList(new ArrayList<>());

        // Assert
        assertThrows(WrongZoneDateException.class, () -> zoneServiceImpl.addZone(zoneReq, event3));
        verify(zoneReq).getZoneCapacity();
        verify(zoneReq, atLeast(1)).getZoneDate();
        verify(zoneReq).getZoneName();
        verify(event3).getDates();
        verify(event3).getZoneList();
        verify(event3).setDates(Mockito.<List<Date>>any());
        verify(event3).setZoneList(Mockito.<List<Zones>>any());
    }

    // listZones() tests
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

    @Test
    void listZones_Empty_Successful() {
        // Arrange
        Event event = mock(Event.class);
        ArrayList<Zones> zonesList = new ArrayList<>();

        // Act
        when(event.getZoneList()).thenReturn(zonesList);
        List<Zones> actualListZonesResult = zoneServiceImpl.listZones(event);

        // Assert
        assertTrue(actualListZonesResult.isEmpty());
        assertSame(zonesList, actualListZonesResult);
    }

    // joinRaffle() and raffle() tests are remaining to be implemented in the future.
    @Test
    void joinRaffle_EventNotFound_ThrowException(){
        //Arrange
        Event event = mock(Event.class);
        when(event.getEventId()).thenReturn(1);
        // Event event = new Event();
        // event.setDates(new ArrayList<>());
        // event.setEventId(1);
        // event.setEventName("Event 1");
        // event.setOpen4Registration(true);
        // EventRepository eventRepository = mock(EventRepository.class);
        // Integer eventId = event.getEventId();

        Zones zones = mock(Zones.class);
        when(zones.getZoneId()).thenReturn(1);
        // Zones zones = new Zones();
        // zones.setEvent(event);
        // zones.setTicketList(new ArrayList<>());
        // zones.setZoneCapacity(1);
        // zones.setZoneDate(new Date());
        // zones.setZoneId(1);
        // zones.setZoneName("Zone 1");
        // Integer zoneId = zones.getZoneId();

        // User user = mock(User.class);
        // when(user.getUsername()).thenReturn("username1");
        // User user = new User();
        // user.setDateOfBirth(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        // user.setEmail("test@gmail.com");
        // user.setPassword("Iloveyou1!");
        // user.setPhoneNumber("1234567890");
        // user.setRole(Role.USER);
        // user.setVerified(true);
        // String username = user.getUsername();

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


}

