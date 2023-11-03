package com.swiftyticket.exceptions;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.swiftyticket.models.Event;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class WrongZoneDateException extends RuntimeException {
    public WrongZoneDateException(Event event, Date zoneDate) {
        super(event.getEventName() + " does not have a performance on " + zoneDate + "!");
    }  
}
