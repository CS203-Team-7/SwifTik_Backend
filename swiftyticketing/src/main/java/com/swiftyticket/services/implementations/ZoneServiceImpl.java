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
import lombok.extern.slf4j.Slf4j;

import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.exceptions.ZoneNotFoundException;


@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneServiceImpl implements ZoneService {
    private final ZoneRepository zoneRepository;
    private final JwtServiceImpl jwtService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

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

        Event joinEvent = joinZone.getEvent();
        if(!joinEvent.getOpenStatus()){
            log.info("user tried to join when pre-registration was closed, denied.");
            return "pre-registration has not yet opened, or pre-registration has closed, join us next time!";
        }

        if(joinEvent.getPreRegisteredUsers4Event().contains(joiningUser)){
            log.info("user tried to join when already pre-registrated, denied.");
            return "you have already pre-registered for this event!";
            
        }

        joinZone.getPreRegisteredUsers4Zone().add(joiningUser);

        joiningUser.getPreRegisteredZones().add(joinZone);
        joiningUser.getPreRegisteredEvents().add(joinEvent);

        //also add user to event they joined so they cant join other zones with the same event
        joinEvent.getPreRegisteredUsers4Event().add(joiningUser);

        eventRepository.save(joinEvent);
        userRepository.save(joiningUser);
        zoneRepository.save(joinZone);

        return "successfully joined the raffle for " + zoneName;
        
    }
}
