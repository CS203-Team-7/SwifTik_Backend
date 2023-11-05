package com.swiftyticket.controllers;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.dto.zone.PreRegisterRequest;
import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;
import com.swiftyticket.services.EventService;
import com.swiftyticket.services.ZoneService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ZoneController {
    private final ZoneService zoneService;
    private final EventService eventService;
    

    public ZoneController(ZoneService zoneService, EventService eventService) {
        this.zoneService = zoneService;
        this.eventService = eventService;
    }

    @PostMapping("/events/{id}/createZone")
    public ResponseEntity<Zones> addZone(@RequestBody @Valid ZoneRequest zoneReq, @PathVariable Integer id){
        Event event = eventService.getEvent(id);
        return new ResponseEntity<Zones> (zoneService.addZone(zoneReq, event), HttpStatus.CREATED);
    }

    @GetMapping("/events/{id}/zones")
    public ResponseEntity<List<Zones>> getZones(@PathVariable Integer id) {
        Event event = eventService.getEvent(id);
        return new ResponseEntity<List<Zones>> (zoneService.listZones(event), HttpStatus.OK);
    }

    @PutMapping("/events/{id}/zone={zoneID}/preRegister")
    public ResponseEntity<String> preRegister(@RequestBody @Valid PreRegisterRequest registerRequest, @PathVariable Integer id, @PathVariable Integer zoneID){
        return new ResponseEntity<String> (zoneService.joinRaffle(registerRequest, id, zoneID), HttpStatus.OK);
    }
    
}
