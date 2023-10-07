package com.swiftyticket.services.implementations;

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

import com.swiftyticket.exceptions.UserNotFoundException;
import com.swiftyticket.exceptions.ZoneNotFoundException;
import com.swiftyticket.exceptions.UserNotFoundException;


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
        //link zone to ZoneWinners
        //newZone.setZoneWinners(new ZoneWinners(newZone));

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
        int toRaffleSize = toRaffle.size();

        //store winning user's id
        //List<Integer> winnerList = new ArrayList<>();
        //store winning users (for use later so we dont have to fetch)
        List<User> userWinners = new ArrayList<>();

        //get number of tickets/seats available (this is how many winners we will be selecting)
        int ticketsLeft = zone.getTicketsLeft();

        //if ticketsLeft > toRaffleSize (means no need to raffle, everyone wins!)
        if(ticketsLeft >= toRaffleSize){
            //clear the pre-registered user list (since all win)
            zone.setPreRegisteredUsers4Zone(new ArrayList<User>());
            userWinners = toRaffle;

            zone.setWinnerList(userWinners);

            zoneRepository.save(zone);

            log.info("no raffling was done - all winners!");
            log.info("" + zone.getWinnerList());

        }else{
            Random rand = new Random();
            //count how many people have won so far, once == zoneCap stop. (no more tickets left to raffle!)
            int count = 0;

            //select winners until there are no tickets left (until ticketsLeft <= count)
            while(ticketsLeft > count){
                //this will roll a number between 0 -> (size of arraylist-1)
                int luckyNumber = rand.nextInt(toRaffle.size());
                User winner = toRaffle.get(luckyNumber);

                userWinners.add(winner);

                //remove winner from next draw, which will in turn update the size. 
                toRaffle.remove(winner);

                log.info("raffled!");
                count++;
            }
            //update zone preRegistered user list.
            zone.setPreRegisteredUsers4Zone(toRaffle);
            //update zone winnerlist
            zone.setWinnerList(userWinners);

            zoneRepository.save(zone);
        }



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
            eventRepository.save(event);
        }

        log.info("ran somehow");
        return;
    }
    
}
