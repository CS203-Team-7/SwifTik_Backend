package com.swiftyticket.services.implementations;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

import com.swiftyticket.exceptions.AlreadyPreRegisteredException;
import com.swiftyticket.exceptions.EventClosedException;
import com.swiftyticket.exceptions.EventNotFoundException;
import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.exceptions.WrongZoneDateException;
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
        //check if event has a date for the zone's date. If event does not have a date for that zone, throw an error (wrong date!)
        if( !(event.getDates().contains(zoneReq.getZoneDate())) ){
            log.info("admin tried to create a zone with a date it's corresponding event does not have!");
            throw new WrongZoneDateException(event, zoneReq.getZoneDate());
        }

        Zones newZone = new Zones(zoneReq.getZoneCapacity(), zoneReq.getZoneName(), zoneReq.getZoneDate(), event);
        newZone = zoneRepository.save(newZone);

        // Add this zone to the event it belongs to
        event.getZoneList().add(newZone);
        eventRepository.save(event);
        return newZone;
    }

    public List<Zones> listZones(Event event){
        return event.getZoneList();
    }

    public String joinRaffle(String bearerToken, Integer eventId, Integer zoneID){
        String jwtToken = bearerToken.substring(7);
        String userEmail = jwtService.extractUserName(jwtToken);
        // get Event and user respectively.
        Event joinEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        User joiningUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("Invalid user / token!"));
        //we search for zone using both event and zoneid to make sure the zone is in the specified event.
        Zones joinZone = zoneRepository.findByZoneIdAndEvent(zoneID, joinEvent).orElseThrow(() -> new ZoneNotFoundException("Invalid zone for " + joinEvent.getEventName()));

        if(!joinEvent.getOpenStatus()){
            log.info("User tried to join when pre-registration was closed, Denied.");
            //return "The Pre-egistration has not yet opened, or Pre-registration has closed, join us next time!";
            throw new EventClosedException();
        }

        if(joinEvent.getPreRegisteredUsers4Event().contains(joiningUser)){
            log.info("User tried to join when already pre-registrated, Denied.");
            //return "You have already pre-registered for this event!";
            throw new AlreadyPreRegisteredException(joinEvent);
            
        }

        joinZone.getPreRegisteredUsers4Zone().add(joiningUser);

        joiningUser.getPreRegisteredZones().add(joinZone);
        joiningUser.getPreRegisteredEvents().add(joinEvent);

        //also add user to event they joined so they cant join other zones with the same event
        joinEvent.getPreRegisteredUsers4Event().add(joiningUser);

        joinZone.setUser_count(joinZone.getPreRegisteredUsers4Zone().size());
        joinEvent.setUser_count(joinEvent.getPreRegisteredUsers4Event().size());

        eventRepository.save(joinEvent);
        userRepository.save(joiningUser);
        zoneRepository.save(joinZone);

        return "Successfully joined the raffle for: " + joinZone.getZoneName() + " on " + joinZone.getZoneDate() + " for " + joinEvent.getEventName();
        
    }

    public void raffle(Zones zone){
        //get associated event with zone
        Event event = zone.getEvent();

        //if raffle round >= 1, means we have to clear the previous round winners.
        if(event.getRaffleRound() >= 1){
            //get previous list of users, clear this zone from their list.
            List<User> previousWinners = zone.getWinnerList();  
            for(User previousWin : previousWinners){
                previousWin.getZonesWon().remove(zone);
            }

            zone.setWinnerList(new ArrayList<User>());
        }

        //get list of users who have pre registered for this zone
        //record size of the list of users.
        List<User> toRaffle = zone.getPreRegisteredUsers4Zone();

        List<User> userWinners = new ArrayList<>();

        //get number of tickets/seats available (this is how many winners we will be selecting)
        int ticketsLeft = zone.getTicketsLeft();

        //if ticketsLeft > toRaffleSize (means no need to raffle, everyone wins!)
        Random rand = new Random();

        //count how many people have won so far, once == zoneCap stop. (no more tickets left to raffle!)
        //int count = 0;

        //select winners until there are no tickets left (until ticketsLeft <= count)
        while(ticketsLeft > userWinners.size() && toRaffle.size()>0){
            //this will roll a number between 0 -> (size of arraylist-1)
            int luckyNumber = rand.nextInt(toRaffle.size());
            User winner = toRaffle.get(luckyNumber);

            userWinners.add(winner);

            //remove winner from next draw, which will in turn update the size. 
            toRaffle.remove(winner);

            log.info("raffled! " + userWinners.size());
            }
        //update zone preRegistered user list.
        zone.setPreRegisteredUsers4Zone(toRaffle);
        //update zone winnerlist
        zone.setWinnerList(userWinners);

        //update count for registered user for zone
        zone.setUser_count(toRaffle.size());

        zoneRepository.save(zone);

        //for all winners, update their user class (and event class) accordingly.
        //event class -> preRegisteredUsers4Event needs to be updated (remove winners)
        //user class -> remove their zone and event pre-registration once they have won. 

        //to do: add a field for users to check which zones they won (store zone id)

        for(int i=0; i<userWinners.size(); i++){
            User u = userWinners.get(i);
            //remove their pre-registration once they won
            u.getPreRegisteredEvents().remove(event);
            u.getPreRegisteredZones().remove(zone);

            //add the zone they won to zoneswon. (to facilitate ticket purchasing later)
            u.getZonesWon().add(zone);

            userRepository.save(u);

            //remove accordingly from event pre-registration list.
            event.getPreRegisteredUsers4Event().remove(u);
            //update count for pre_registered users for an event.
            event.setUser_count(event.getPreRegisteredUsers4Event().size());
            eventRepository.save(event);

            log.info("winner no." + i + " zones won" + u.getZonesWon());
            log.info("winner no." + i + " preRegisteredZones" + u.getPreRegisteredZones());
            log.info("winner no." + i + " preRegisteredEvent" + u.getPreRegisteredEvents());
        }

        //check everything is updated accordingly.
        log.info("event preResigtered list: " + event.getPreRegisteredUsers4Event());
        log.info("zone preRegistered list: " + zone.getPreRegisteredUsers4Zone());
        log.info("zone winner list:" + zone.getWinnerList());

        log.info("ran somehow");
        return;
    }
    
}
