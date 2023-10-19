package com.swiftyticket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.swiftyticket.models.Event;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AlreadyPreRegisteredException extends RuntimeException {
    public AlreadyPreRegisteredException(Event event) {
        super("you have already registered for this event: " + event.getEventName());
    }  
}
