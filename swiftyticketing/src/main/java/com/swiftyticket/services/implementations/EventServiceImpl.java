package com.swiftyticket.services.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.exceptions.OpenRegistrationRaffleException;
import com.swiftyticket.models.Event;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.services.EventService;



@Service
public class EventServiceImpl implements EventService{
    private EventRepository eventRepository;
    private ZoneServiceImpl zoneService;

    public EventServiceImpl(EventRepository eventRepository, ZoneServiceImpl zoneService) {
        this.eventRepository = eventRepository;
        this.zoneService = zoneService;
    }

    /**
     * Returns a list of all events in the DB.
     * @return List<Event>
     */
    @Override
    public List<Event> listEvents() {
        return eventRepository.findAll();
    }

    /**
     * Returns a single event based on the event ID.
     * @param id -> Integer event ID (Unique identifier)
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     * @return Event -> Event object with the specified ID
     */
    // Consider adding get methods by date, artist, genre. KIV for future business logic
    @Override
    public Event getEvent(Integer id) {
        return eventRepository.findById(id).map(event -> {
            return event;
        }).orElseThrow(() -> new EventNotFoundException(id));
    }

    /**
     * Adds a new event to the DB from the event details provided.
     * @param event -> Event object containing the event details
     * @return Event -> Event object that was added to the DB
     */
    @Override 
    public Event addEvent(Event event) {
        event.setZoneList(new ArrayList<>());
        event.setPreRegisteredUsers4Event(new ArrayList<>());
        return eventRepository.save(event);
    }

    /**
     * Updates an existing event in the DB with the new event details provided.
     * @param id -> Integer event ID (Unique identifier)
     * @param newEventInfo -> Event object containing the (updated) event details
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     * @return Event -> Event object that was updated in the DB
     */
    @Override
    public Event updateEvent(Integer id, Event newEventInfo) {
        return eventRepository.findById(id).map(event -> {
            event.setEventName(newEventInfo.getEventName());
            event.setArtists(newEventInfo.getArtists());
            event.setDates(newEventInfo.getDates());
            event.setVenue(newEventInfo.getVenue());
            event.setVenueCapacity(newEventInfo.getVenueCapacity());
            return eventRepository.save(event);
        }).orElseThrow(() -> new EventNotFoundException(id));
    }

    /**
     * Deletes an existing event from the DB.
     * @param id -> Integer event ID (Unique identifier)
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     */
    @Override
    public void deleteEvent(Integer id) {
        Optional<Event> e = eventRepository.findById(id);
        if (e == null) throw new EventNotFoundException(id);

        Event event = e.get();
        eventRepository.deleteById(event.getEventId());
    }

    /**
     * Opens up an event so that users can pre-register for zones in the event.
     * @param id -> Integer event ID (Unique identifier)
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     */
    public void openEvent(Integer id){
        Optional<Event> e = eventRepository.findById(id);
        if (e == null) throw new EventNotFoundException(id);

        Event event = e.get();
        event.setOpen4Registration(true);
        eventRepository.save(event);
    }

    /**
     * Closes an event so that users can no more pre-register for zones in the event.
     * @param id -> Integer event ID (Unique identifier)
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     */
    public void closeEvent(Integer id){
        Optional<Event> e = eventRepository.findById(id);
        if (e == null) throw new EventNotFoundException(id);

        Event event = e.get();
        event.setOpen4Registration(false);
        eventRepository.save(event);
    }

    /**
     * This function is to perform the raffling of each zone in the event once pre-registration is closed.
     * @param id -> Integer event ID (Unique identifier)
     * @throws EventNotFoundException -> if the event ID does not exist in the DB
     * @throws OpenRegistrationRaffleException -> if the event is still open for registration
     */
    public void raffle(Integer id){
        Optional<Event> e = eventRepository.findById(id);
        if (e == null) throw new EventNotFoundException(id);
        Event event = e.get();

        //to do: make sure event is closed before raffle is commenced.
        if(event.getOpenStatus()){
            throw new OpenRegistrationRaffleException("please close the event before raffling.");
        }

        for(int i=0; i<event.getZoneList().size(); i++){
            zoneService.raffle(event.getZoneList().get(i));
            //log.info("entered event raffle's for loop");
        }

        event.setRaffleRound(event.getRaffleRound() + 1);
        //log.info("event's raffle round: " + event.getRaffleRound());

        eventRepository.save(event);

        return;
    }
}
