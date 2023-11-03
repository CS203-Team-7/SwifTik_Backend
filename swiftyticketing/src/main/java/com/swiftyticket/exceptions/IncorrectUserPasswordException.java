package com.swiftyticket.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class IncorrectUserPasswordException extends RuntimeException{
    public IncorrectUserPasswordException() {
        super("Invalid email/password!");
    }
}
