package com.swiftyticket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalSignUpArgumentException.class)
    public ResponseEntity<Object> handleIllegalSignUpArgumentException(IllegalSignUpArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JWTExpiredException.class)
    public ResponseEntity<Object> handleJWTExpiredException(JWTExpiredException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(ZoneNotFoundException.class)
    public ResponseEntity<Object> handleZoneNotFoundException(ZoneNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
    
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

    @ExceptionHandler(AlreadyPreRegisteredException.class)
    public ResponseEntity<Object> AlreadyPreRegisteredException(AlreadyPreRegisteredException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EventClosedException.class)
    public ResponseEntity<Object> EventClosedException(EventClosedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(PurchaseException.class)
    public ResponseEntity<Object> PurchaseException(PurchaseException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<Object> TicketNotFoundException(TicketNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Object> EventNotFoundException(EventNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Object> DuplicateUserException(DuplicateUserException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Will add exception handler for payment later
}
