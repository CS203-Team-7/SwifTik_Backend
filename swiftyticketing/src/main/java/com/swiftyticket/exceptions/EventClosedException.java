package com.swiftyticket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class EventClosedException extends RuntimeException {
    public EventClosedException() {
        super("The Pre-egistration has not yet opened, or Pre-registration has closed, join us next time!");
    }  
}
