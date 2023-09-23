package com.swiftyticket.services.implementations;

import java.util.List;
import org.springframework.stereotype.Service;
import com.swiftyticket.dto.zone.ZoneRequest;
import com.swiftyticket.models.Event;
import com.swiftyticket.models.User;
import com.swiftyticket.models.Zones;
import com.swiftyticket.repositories.EventRepository;
import com.swiftyticket.repositories.UserRepository;
import com.swiftyticket.repositories.ZoneRepository;
import com.swiftyticket.services.ZoneService;
import lombok.RequiredArgsConstructor;
import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.exceptions.ZoneNotFoundException;


@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {
    private final ZoneRepository zoneRepository;
    private final JwtServiceImpl jwtService;
    private final UserRepository userRepository;
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

    public String joinRaffle(String bearerToken, Integer id, String zoneName){
        String jwtToken = bearerToken.substring(7);
        String userEmail = jwtService.extractUserName(jwtToken);
        //get user using userEmail
        User joiningUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("invalid user / token!"));
        Zones joinZone = zoneRepository.findByZoneName(zoneName).orElseThrow(() -> new ZoneNotFoundException("invalid zone!"));

        joinZone.getPreRegisteredUsers4Zone().add(joiningUser);
        joiningUser.getPreRegisteredZones().add(joinZone);

        return "successfully joined the raffle for " + zoneName;
        
    }
}
