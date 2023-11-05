package com.swiftyticket.controllers;

import java.util.List;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {
    private EventService eventService;

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
    public ResponseEntity<Event> addEvent(@RequestBody Event event){
        return new ResponseEntity<Event>(eventService.addEvent(event), HttpStatus.CREATED);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Integer id, @RequestBody Event newEventInfo) throws EventNotFoundException {
        Event event = eventService.updateEvent(id, newEventInfo);
        if (event == null) throw new EventNotFoundException(id);
        return new ResponseEntity<Event>(event, HttpStatus.OK);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Integer id) {
        try {
            // DEBUGGING
            log.info("DEBUG: entering try block in deleteEvent method in EventController");

            eventService.deleteEvent(id);

            // DEUBUGGING
            log.info("DEBUG: passed try block in deleteEvent method in EventController");
        } catch(Exception e) {
            // DEUBUGGING
            log.info("DEBUG: entering catch block in deleteEvent method in EventController");

            throw new EventNotFoundException(id);
        }
        // DEUBUGGING
        log.info("DEBUG: exiting deleteEvent method in EventController");

        return new ResponseEntity<String>("Event #" + id + " has been deleted.", HttpStatus.OK);
    }

    @PutMapping("/events/{id}/close")
    public ResponseEntity<String> closeRegistration(@PathVariable Integer id)  {
        try {
            eventService.closeEvent(id);
        } catch (Exception e) {
            throw new EventNotFoundException(id);
        }
        return new ResponseEntity<String>("Event #" + id + "'s registration window has been closed.", HttpStatus.OK);
    }

    @PutMapping("/events/{id}/open")
    public ResponseEntity<String> openRegistration(@PathVariable Integer id) {
        try {
            eventService.openEvent(id);
        } catch (Exception e) {
            throw new EventNotFoundException(id);
        }
        return new ResponseEntity<String>("Event #" + id + "'s registration window has been opened.", HttpStatus.OK);
    }

    @PutMapping("/events/{id}/raffle")
    public ResponseEntity<String> eventRaffle(@PathVariable Integer id) {
        try {
            eventService.raffle(id);
        } catch (Exception e) {
            throw new EventNotFoundException(id);
        }
        return new ResponseEntity<String>("Event #" + id + "'s raffle has been done", HttpStatus.OK);
    }
}
