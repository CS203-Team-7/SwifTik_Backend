package com.swiftyticket.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zone;
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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/events/{id}/createZone")
    public void addEvent(@RequestBody ZoneRequest zoneReq, @PathVariable Integer id){
        Event event = eventService.getEvent(id);
        zoneService.addZone(zoneReq, event);
    }

    @GetMapping("/events/{id}/zones")
    public List<Zone> getZones(@PathVariable Integer id) {
        Event event = eventService.getEvent(id);
        return zoneService.listZones(event);
    }
}
