package com.swiftyticket.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;
import com.swiftyticket.services.EventService;
import com.swiftyticket.services.ZoneService;

@RestController
public class ZoneController {
    private ZoneService zoneService;
    private EventService eventService;
    

    public ZoneController(ZoneService zoneService, EventService eventService) {
        this.zoneService = zoneService;
        this.eventService = eventService;
    }

    @PostMapping("/events/{id}/createZone")
    public ResponseEntity<Zones> addEvent(@RequestBody ZoneRequest zoneReq, @PathVariable Integer id){
        Event event = eventService.getEvent(id);
        return new ResponseEntity<Zones> (zoneService.addZone(zoneReq, event), HttpStatus.CREATED);
    }

    @GetMapping("/events/{id}/zones")
    public ResponseEntity<List<Zones>> getZones(@PathVariable Integer id) {
        Event event = eventService.getEvent(id);
        return new ResponseEntity<List<Zones>> (zoneService.listZones(event), HttpStatus.OK);
    }

    @PutMapping("/events/{id}/{zoneName}/preRegister")
    public ResponseEntity<String> preRegister(@RequestHeader("Authorization") String bearerToken, @PathVariable Integer id, @PathVariable String zoneName){
        return new ResponseEntity<String> (zoneService.joinRaffle(bearerToken, id, zoneName), HttpStatus.OK);
    }
    
}
