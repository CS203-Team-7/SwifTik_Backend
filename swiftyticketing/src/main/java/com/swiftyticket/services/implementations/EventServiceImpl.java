package com.swiftyticket.services.implementations;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.models.Event;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.services.EventService;



@Service
public class EventServiceImpl implements EventService{
    private EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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
        }).orElse(null);
    }

    @Override 
    public Event addEvent(Event event) {
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
        }).orElse(null);
    }

    @Override
    public void deleteEvent(Integer id) {
        Optional<Event> e = eventRepository.findById(id);
        if (e == null) throw new EventNotFoundException(id);

        Event event = e.get();
        eventRepository.deleteById(event.getEventId());
    }

    public void openEvent(Integer id){
        Optional<Event> e = eventRepository.findById(id);
        if (e == null) throw new EventNotFoundException(id);

        Event event = e.get();
        event.setOpen4Registration(true);
        eventRepository.save(event);
    }

    public void closeEvent(Integer id){
        Optional<Event> e = eventRepository.findById(id);
        if (e == null) throw new EventNotFoundException(id);

        Event event = e.get();
        event.setOpen4Registration(false);
        eventRepository.save(event);
    }
}
