package com.swiftyticket.exceptions;

public class IllegalSignUpArgumentException extends RuntimeException {
    public IllegalSignUpArgumentException(String message) {
        super(message);
    }
}
