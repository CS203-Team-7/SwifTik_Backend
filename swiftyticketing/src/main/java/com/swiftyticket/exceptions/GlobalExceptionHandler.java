package com.swiftyticket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>("Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number and one special character", HttpStatus.BAD_REQUEST);
    } 

    @ExceptionHandler(IncorrectUserPasswordException.class)
    public ResponseEntity<Object> handleIncorrectUserPasswordException(IncorrectUserPasswordException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<Object> AccountNotVerifiedException(AccountNotVerifiedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OpenRegistrationRaffleException.class)
    public ResponseEntity<Object> OpenRegistrationRaffleException(OpenRegistrationRaffleException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(WrongZoneDateException.class)
    public ResponseEntity<Object> WrongZoneDateException(WrongZoneDateException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    //Will add exception handler for payment later
}
