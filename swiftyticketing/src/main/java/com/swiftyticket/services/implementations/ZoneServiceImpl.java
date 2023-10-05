package com.swiftyticket.services.implementations;

import java.util.ArrayList;
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
        // Add this zone to the event it belongs to
        event.getZoneList().add(newZone);
        return zoneRepository.save(newZone);
    }

    public List<Zones> listZones(Event event){

        return event.getZoneList();
    }

    public String joinRaffle(String bearerToken, Integer id, String zoneName){
        String jwtToken = bearerToken.substring(7);
        String userEmail = jwtService.extractUserName(jwtToken);
        // Get user using userEmail
        User joiningUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("Invalid user / token!"));
        Zones joinZone = zoneRepository.findByZoneName(zoneName).orElseThrow(() -> new ZoneNotFoundException("Invalid zone!"));

        Event joinEvent = joinZone.getEvent();
        if(!joinEvent.getOpenStatus()){
            log.info("User tried to join when pre-registration was closed, Denied.");
            return "The Pre-egistration has not yet opened, or Pre-registration has closed, join us next time!";
        }

        if(joinEvent.getPreRegisteredUsers4Event().contains(joiningUser)){
            log.info("User tried to join when already pre-registrated, Denied.");
            return "You have already pre-registered for this event!";
            
        }

        joinZone.getPreRegisteredUsers4Zone().add(joiningUser);

        joiningUser.getPreRegisteredZones().add(joinZone);
        joiningUser.getPreRegisteredEvents().add(joinEvent);

        //also add user to event they joined so they cant join other zones with the same event
        joinEvent.getPreRegisteredUsers4Event().add(joiningUser);

        eventRepository.save(joinEvent);
        userRepository.save(joiningUser);
        zoneRepository.save(joinZone);

        return "Successfully joined the raffle for: " + zoneName;
        
    }

    public void raffle(Zones zone){

        //get list of users who have pre registered for this zone
        //record size of the list of users.
        List<User> toRaffle = zone.getPreRegisteredUsers4Zone();
        int toRaffleSize = toRaffle.size();

        //get number of tickets/seats available (this is how many winners we will be selecting)
        int zoneCap = zone.getZoneCapacity();

        //if zoneCap >= toRaffleSize (means no need to raffle, everyone wins!)
        if(zoneCap >= toRaffleSize){
            zone.setZoneWinners(toRaffle);
            zone.setPreRegisteredUsers4Zone(new ArrayList<User>());
            zoneRepository.save(zone);
        }else{

        }

        //get associated event with zone
        Event event = zone.getEvent();

        for(User u : zone.getZoneWinners()){
            //remove their pre-registration once they won
            u.getPreRegisteredEvents().remove(event);
            u.getPreRegisteredZones().remove(zone);

            //add the zone they won to zoneswon. (to facilitate ticket purchasing later)
            u.getZonesWon().add(zone);

            userRepository.save(u);

            //remove accordingly from event pre-registration list.
            event.getPreRegisteredUsers4Event().remove(u);
            eventRepository.save(event);
        }

        //remove all winners from 

        //create a for/while loop, each loop will random between 0->(arraysize-1), update array size and array per loop
        //for each winner, remove them from the preRegistration4Zones, preRegistration4events arrays and update all accordingly.
        //add them to the winners array, and update their zonesWon element. 
        log.info("raffled!");
        return;
    }
}
