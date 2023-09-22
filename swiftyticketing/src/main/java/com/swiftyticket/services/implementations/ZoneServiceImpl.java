package com.swiftyticket.services.implementations;

import java.util.List;
import org.springframework.stereotype.Service;
import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.ZoneRepository;
import com.swiftyticket.services.ZoneService;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {
    private final ZoneRepository zoneRepository;
    //private final EventRepository eventRepository;

    public Zones addZone(ZoneRequest zoneReq, Event event){
        Zones newZone = new Zones(zoneReq.getZoneCapacity(), zoneReq.getZoneName(), event);
        //add this zone to the event it belongs to
        event.getZoneList().add(newZone);
        return zoneRepository.save(newZone);
    }

    public List<Zones> listZones(Event event){
        return zoneRepository.findAll();
    }
}
