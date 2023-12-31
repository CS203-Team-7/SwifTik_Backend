package com.swiftyticket.services;

import java.util.List;

import com.swiftyticket.dto.zone.PreRegisterRequest;
import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;

public interface ZoneService {
    public Zones addZone(ZoneRequest zoneReq, Event event);
    public List<Zones> listZones(Event event);
    public String joinRaffle(PreRegisterRequest registerRequest, Integer id, Integer zoneID);
    public void raffle(Zones zone);
    public List<Zones> userJoinedZones(String userEmail);
    public Event getCorrespondingEvent(Integer zoneID);
}
