package com.swiftyticket.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class NewController {
    @GetMapping
    public ResponseEntity<String> message(){
        return new ResponseEntity<>("Authenticated", HttpStatus.OK);
    }
}
