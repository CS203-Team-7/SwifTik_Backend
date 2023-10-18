package com.swiftyticket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PurchaseException extends RuntimeException {
    public PurchaseException() {
        super("Either you have not won a raffle for this zone, or you have already bought a ticket.");
    }  
}
