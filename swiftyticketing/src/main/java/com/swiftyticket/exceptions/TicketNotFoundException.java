package com.swiftyticket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 404 error
public class TicketNotFoundException extends RuntimeException {

    // This was included in the class activity, but idk what it's for
    private static final long serialVersionUID = 1L;

    public TicketNotFoundException(Integer id) {
        super("Could not find ticket " + id);
    }

}
