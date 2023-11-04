package com.swiftyticket;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.exceptions.WrongZoneDateException;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.repositories.ZoneRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
}

