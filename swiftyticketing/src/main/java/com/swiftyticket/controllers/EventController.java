package com.swiftyticket.controllers;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.models.Event;
import com.swiftyticket.services.EventService;

@RestController
public class EventController {
    private EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Possible improvements: get by artist, event name, date, venue, genre (genre would need a new attribute in Event)

    @GetMapping("/events")
    public List<Event> getEvents() {
        return eventService.listEvents();
    }

    @GetMapping("/events/{id}")
    public Event findEvent(@PathVariable Integer id) {
        Event event = eventService.getEvent(id);

        if (event == null) throw new EventNotFoundException(id);
        return event;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/events")
    public void addEvent(@RequestBody Event event){
        eventService.addEvent(event);
    }

    @PutMapping("/events/{id}")
    public Event updateEvent(@PathVariable Integer id, @RequestBody Event newEventInfo) throws EventNotFoundException {
        Event event = eventService.updateEvent(id, newEventInfo);
        if (event == null) throw new EventNotFoundException(id);
        return event;
    }

    @DeleteMapping("/events/{id}")
    public String deleteEvent(@PathVariable Integer id) {
        try {
            eventService.deleteEvent(id);
        } catch(EmptyResultDataAccessException e) {
            throw new EventNotFoundException(id);
        }
        return "Event #" + id + " has been deleted.";
    }
}
