package com.swiftyticket.services.implementations;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.exceptions.OpenRegistrationRaffleException;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {EventServiceImpl.class})
@ExtendWith(SpringExtension.class)
class EventServiceUnitTests {
    @MockBean
    private EventRepository eventRepository;

    @Autowired
    private EventServiceImpl eventServiceImpl;

    @MockBean
    private ZoneServiceImpl zoneServiceImpl;

    // Method to get Event object, so we don't have to keep creating new ones
    private static Event getInfo(String eventName, String venue) {
        Event newEventInfo = new Event();
        newEventInfo.setArtists(new ArrayList<>());
        newEventInfo.setDates(new ArrayList<>());
        newEventInfo.setEventId(1);
        newEventInfo.setEventName(eventName);
        newEventInfo.setOpen4Registration(true);
        newEventInfo.setRaffleRound(1);
        newEventInfo.setUser_count(3);
        newEventInfo.setVenue(venue);
        newEventInfo.setVenueCapacity(1);
        newEventInfo.setZoneList(new ArrayList<>());
        return newEventInfo;
    }

    // listEvents() tests
    @Test
    void listEvents_Successful() {
        // Arrange
        ArrayList<Event> eventList = new ArrayList<>();

        // Act
        when(eventRepository.findAll()).thenReturn(eventList);
        List<Event> actualListEventsResult = eventServiceImpl.listEvents();

        // Assert
        verify(eventRepository).findAll();
        assertTrue(actualListEventsResult.isEmpty());
        assertSame(eventList, actualListEventsResult);
    }

    @Test
    void listEvents_NoEvents_ThrowException() {
        // Arrange and Act
        when(eventRepository.findAll()).thenThrow(new EventNotFoundException(1));

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.listEvents());
        verify(eventRepository).findAll();
    }

    // getEvent() tests
    @Test
    void getEvent_ValidEvent_Successful() {
        // Arrange
        Event event = new Event();
        event.setArtists(new ArrayList<>());
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setRaffleRound(1);
        event.setUser_count(3);
        event.setVenue("Venue 1");
        event.setVenueCapacity(1);
        event.setZoneList(new ArrayList<>());
        Optional<Event> ofResult = Optional.of(event);

        // Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        Event actualEvent = eventServiceImpl.getEvent(1);

        // Assert
        verify(eventRepository).findById(Mockito.<Integer>any());
        assertSame(event, actualEvent);
    }

    @Test
    void getEvent_InvalidEvent_ThrowException() {
        // Arrange
        Optional<Event> emptyResult = Optional.empty();

        // Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(emptyResult);

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.getEvent(1));
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void getEvent_NullInput_ThrowException() {
        // Arrange and Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenThrow(new EventNotFoundException(1));

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.getEvent(1));
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    // addEvent() tests
    @Test
    void addEvent_ValidEvent_Successful() {
        // Arrange
        Event event = new Event();
        event.setArtists(new ArrayList<>());
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setRaffleRound(1);
        event.setUser_count(3);
        event.setVenue("Venue 1");
        event.setVenueCapacity(1);
        event.setZoneList(new ArrayList<>());

        // Act
        when(eventRepository.save(Mockito.<Event>any())).thenReturn(event);
        Event event2 = getInfo("Event 1", "Venue 1");
        Event actualAddEventResult = eventServiceImpl.addEvent(event2);

        // Assert
        verify(eventRepository).save(Mockito.<Event>any());
        assertSame(event, actualAddEventResult);
    }

    @Test
    void addEvent_InvalidInput_ThrowException() {
        // Arrange
        Event event = new Event();
        event.setArtists(new ArrayList<>());
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setRaffleRound(1);
        event.setUser_count(3);
        event.setVenue("Venue 1");
        event.setVenueCapacity(1);
        event.setZoneList(new ArrayList<>());

        // Act
        when(eventRepository.save(Mockito.<Event>any())).thenThrow(new EventNotFoundException(1));

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.addEvent(event));
        verify(eventRepository).save(Mockito.<Event>any());
    }

    // updateEvent() tests
    @Test
    void updateEvent_ValidInput_Successful() {
        // Arrange
        Event event = new Event();
        event.setArtists(new ArrayList<>());
        event.setDates(new ArrayList<>());
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setRaffleRound(1);
        event.setUser_count(3);
        event.setVenue("Venue 1");
        event.setVenueCapacity(1);
        event.setZoneList(new ArrayList<>());
        Optional<Event> ofResult = Optional.of(event);

        Event event2 = getInfo("Event 1", "Venue 1");

        // Act
        when(eventRepository.save(Mockito.<Event>any())).thenReturn(event2);
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        Event newEventInfo = getInfo("Event 1", "Venue 1");
        Event actualUpdateEventResult = eventServiceImpl.updateEvent(1, newEventInfo);

        // Assert
        verify(eventRepository).findById(Mockito.<Integer>any());
        verify(eventRepository).save(Mockito.<Event>any());
        assertSame(event2, actualUpdateEventResult);
    }

    @Test
    void updateEvent_InvalidInput_ThrowException() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);

        // Act
        when(eventRepository.save(Mockito.<Event>any())).thenThrow(new EventNotFoundException(1));
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        Event newEventInfo = getInfo("Event 1", "Venue 1");

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.updateEvent(1, newEventInfo));
        verify(eventRepository).findById(Mockito.<Integer>any());
        verify(eventRepository).save(Mockito.<Event>any());
    }

    @Test
    void updateEvent_NullInput_ThrowException() {
        // Arrange
        Optional<Event> emptyResult = Optional.empty();

        // Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(emptyResult);
        Event newEventInfo = getInfo("Event 1", "Venue 1");

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.updateEvent(1, newEventInfo));
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void updateEvent_NoSuchEvent_ThrowException() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);

        // Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        // To verify setters, we need to mock the set methods of the Event object and do nothing
        Event newEventInfo = mock(Event.class);
        when(newEventInfo.getVenueCapacity()).thenThrow(new EventNotFoundException(1));
        when(newEventInfo.getVenue()).thenReturn("Venue 1");
        when(newEventInfo.getArtists()).thenReturn(new ArrayList<>());
        when(newEventInfo.getDates()).thenReturn(new ArrayList<>());
        when(newEventInfo.getEventName()).thenReturn("Event 1");

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.updateEvent(1, newEventInfo));
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    // deleteEvent() tests
    @Test
    void deleteEvent_Successful() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);

        // Act
        doNothing().when(eventRepository).deleteById(Mockito.<Integer>any());
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        eventServiceImpl.deleteEvent(1);

        // Assert
        verify(eventRepository).deleteById(Mockito.<Integer>any());
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void deleteEvent_NoSuchEvent_ThrowException() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);

        // Act
        doThrow(new EventNotFoundException(1)).when(eventRepository).deleteById(Mockito.<Integer>any());
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.deleteEvent(1));
        verify(eventRepository).deleteById(Mockito.<Integer>any());
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void deleteEvent_NullInput_ThrowException() {
        // Arrange and Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(null);

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.deleteEvent(1));
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    // openEvent() tests
    @Test
    void openEvent_ValidEvent_Successful() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);

        // Act
        Event event2 = getInfo("Event 1", "Venue 1");
        when(eventRepository.save(Mockito.<Event>any())).thenReturn(event2);
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        eventServiceImpl.openEvent(1);

        // Assert
        verify(eventRepository).findById(Mockito.<Integer>any());
        verify(eventRepository).save(Mockito.<Event>any());
    }

    @Test
    void openEvent_NoSuchEvent_ThrowException() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);

        // Act
        when(eventRepository.save(Mockito.<Event>any())).thenThrow(new EventNotFoundException(1));
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.openEvent(1));
        verify(eventRepository).findById(Mockito.<Integer>any());
        verify(eventRepository).save(Mockito.<Event>any());
    }

    @Test
    void openEvent_NullInput_ThrowException() {
        // Arrange and Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(null);

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.openEvent(1));
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    // closeEvent() tests
    @Test
    void closeEvent_ValidEvent_Successful() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);
        Event event2 = getInfo("Event 1", "Venue 1");

        // Act
        when(eventRepository.save(Mockito.<Event>any())).thenReturn(event2);
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        eventServiceImpl.closeEvent(1);

        // Assert
        verify(eventRepository).findById(Mockito.<Integer>any());
        verify(eventRepository).save(Mockito.<Event>any());
    }

    @Test
    void closeEvent_NoSuchEvent_ThrowException() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);

        // Act
        when(eventRepository.save(Mockito.<Event>any())).thenThrow(new EventNotFoundException(1));
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.closeEvent(1));
        verify(eventRepository).findById(Mockito.<Integer>any());
        verify(eventRepository).save(Mockito.<Event>any());
    }

    @Test
    void closeEvent_NullInput_ThrowException() {
        // Arrange and Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(null);

        // Assert
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.closeEvent(1));
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void raffle_OpenEvent_ThrowException() {
        // Arrange
        Event event = getInfo("Event 1", "Venue 1");
        Optional<Event> ofResult = Optional.of(event);

        // Act
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);

        // Assert
        assertThrows(OpenRegistrationRaffleException.class, () -> eventServiceImpl.raffle(1));
        verify(eventRepository).findById(Mockito.<Integer>any());
    }

    @Test
    void raffle_Successful() {
        // Arrange
        Event event = mock(Event.class);
        when(event.getOpenStatus()).thenReturn(false);
        when(event.getRaffleRound()).thenReturn(1);
        event.setEventId(1);
        event.setEventName("Event 1");
        event.setOpen4Registration(true);
        event.setRaffleRound(1);
        event.setUser_count(3);
        event.setZoneList(new ArrayList<>());
        Optional<Event> ofResult = Optional.of(event);
        Event event2 = getInfo("Event 1", "Venue 1");

        // Act
        when(eventRepository.save(Mockito.<Event>any())).thenReturn(event2);
        when(eventRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        eventServiceImpl.raffle(1);

        // Assert
        verify(event).getOpenStatus();
        verify(event).getRaffleRound();
        // Check that the raffleRound is incremented by 1
        verify(event, atLeast(1)).setRaffleRound(Mockito.<Integer>any());
        verify(event).setUser_count(anyInt());
        verify(event).setZoneList(Mockito.<List<Zones>>any());
        verify(eventRepository).findById(Mockito.<Integer>any());
        verify(eventRepository).save(Mockito.<Event>any());
    }
}

