package com.swiftyticket.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class JWTExpiredException extends RuntimeException{
    public JWTExpiredException(String message) {
        super(message);
    }
}
