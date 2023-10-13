package com.swiftyticket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends RuntimeException{
    public PaymentNotFoundException(Integer id) {
        super("Could not find payment " + id);
    }
}

