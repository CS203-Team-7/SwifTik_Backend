package com.swiftyticket.controllers;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.models.Event;
import com.swiftyticket.services.EventService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Possible improvements: get by artist, event name, date, venue, genre (genre would need a new attribute in Event)

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getEvents() {
        return new ResponseEntity<List<Event>> (eventService.listEvents(), HttpStatus.OK);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> findEvent(@PathVariable Integer id) {
        Event event = eventService.getEvent(id);

        if (event == null) throw new EventNotFoundException(id);
        return new ResponseEntity<Event> (event, HttpStatus.OK);
    }

    @PostMapping("/events/create")
    public ResponseEntity<Event> addEvent(@RequestBody @Valid Event event){
        return new ResponseEntity<Event>(eventService.addEvent(event), HttpStatus.CREATED);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Integer id, @RequestBody @Valid Event newEventInfo) throws EventNotFoundException {
        Event event = eventService.updateEvent(id, newEventInfo);
        if (event == null) throw new EventNotFoundException(id);
        return new ResponseEntity<Event>(event, HttpStatus.OK);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Integer id) {
        try {
            eventService.deleteEvent(id);
        } catch(EmptyResultDataAccessException e) {
            throw new EventNotFoundException(id);
        }
        return new ResponseEntity<String>("Event #" + id + " has been deleted.", HttpStatus.OK);
    }

    @PutMapping("/events/{id}/close")
    public ResponseEntity<String> closeRegistration(@PathVariable Integer id) throws EventNotFoundException {
        eventService.closeEvent(id);
        return new ResponseEntity<String>("Event #" + id + "'s registration window has been closed.", HttpStatus.OK);
    }

    @PutMapping("/events/{id}/open")
    public ResponseEntity<String> openRegistration(@PathVariable Integer id) throws EventNotFoundException {
        eventService.openEvent(id);
        return new ResponseEntity<String>("Event #" + id + "'s registration window has been opened.", HttpStatus.OK);
    }

    @PutMapping("/events/{id}/raffle")
    public ResponseEntity<String> eventRaffle(@PathVariable Integer id) throws EventNotFoundException {
        eventService.raffle(id);
        return new ResponseEntity<String>("Event #" + id + "'s raffle has been done", HttpStatus.OK);
    }
}
