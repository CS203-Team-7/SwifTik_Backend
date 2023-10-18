package com.swiftyticket.services;

import java.util.List;

import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;

public interface ZoneService {
    public Zones addZone(ZoneRequest zoneReq, Event event);
    public List<Zones> listZones(Event event);
    public String joinRaffle(String bearerToken, Integer id, Integer zoneID);
    public void raffle(Zones zone);
}
