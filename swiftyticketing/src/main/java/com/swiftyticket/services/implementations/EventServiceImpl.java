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

    @Override
    public List<Event> listEvents() {
        return eventRepository.findAll();
    }

    // Consider adding get methods by date, artist, genre. KIV for future business logic
    @Override
    public Event getEvent(Integer id) {
        return eventRepository.findById(id).map(event -> {
            return event;
        }).orElseThrow(() -> new EventNotFoundException(id));
    }

    @Override 
    public Event addEvent(Event event) {
        event.setZoneList(new ArrayList<>());
        event.setPreRegisteredUsers4Event(new ArrayList<>());
        return eventRepository.save(event);
    }

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

    @Override
    public void deleteEvent(Integer id) {
        //check that event exists
        Event e = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        // if exists error won't be thrown and we can proceed to delete (yipee)
        eventRepository.deleteById(id);
    }

    public void openEvent(Integer id){
        //check the event exists
        Event e = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        // if exists error won't be thrown and we can proceed to update (yipee)
        e.setOpen4Registration(true);
        eventRepository.save(e);
    }

    public void closeEvent(Integer id){
        //check the event exists
        Event e = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        // if exists error won't be thrown and we can proceed to update (yipee)
        e.setOpen4Registration(false);
        eventRepository.save(e);
    }

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
