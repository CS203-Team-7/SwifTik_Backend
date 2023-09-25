package com.swiftyticket.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalSignUpArgumentException extends RuntimeException {
    public IllegalSignUpArgumentException(String message) {
        super(message);
    }
}
