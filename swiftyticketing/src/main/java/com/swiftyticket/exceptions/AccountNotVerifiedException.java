package com.swiftyticket.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccountNotVerifiedException extends RuntimeException{
    public AccountNotVerifiedException(String message) {
        super(message);
    }
}
