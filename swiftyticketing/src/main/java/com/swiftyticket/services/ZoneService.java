package com.swiftyticket.services;

import java.util.List;

import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zone;

public interface ZoneService {
    public Zone addZone(ZoneRequest zoneReq, Event event);
    public List<Zone> listZones(Event event);
}
