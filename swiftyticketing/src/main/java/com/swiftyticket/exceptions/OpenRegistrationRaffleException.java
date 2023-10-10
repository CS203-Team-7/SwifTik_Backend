package com.swiftyticket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OpenRegistrationRaffleException extends RuntimeException {
    public OpenRegistrationRaffleException(String message) {
        super(message);
    }  
}
